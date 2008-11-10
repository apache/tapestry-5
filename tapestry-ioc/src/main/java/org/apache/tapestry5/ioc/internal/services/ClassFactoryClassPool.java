// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import javassist.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFabUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

/**
 * Used to ensure that {@link javassist.ClassPool#appendClassPath(javassist.ClassPath)} is invoked within a synchronized
 * lock, and also handles tricky class loading issues (caused by the creation of classes, and class loaders, at
 * runtime).
 */
public class ClassFactoryClassPool extends ClassPool
{

    // Kind of duplicating some logic from ClassPool to avoid a deadlock-producing synchronized block.

    private static final Method defineClass = findMethod("defineClass", String.class, byte[].class,
                                                         int.class, int.class);

    private static final Method defineClassWithProtectionDomain = findMethod("defineClass", String.class, byte[].class,
                                                                             int.class, int.class,
                                                                             ProtectionDomain.class);

    private static Method findMethod(final String methodName, final Class... parameterTypes)
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>()
            {
                public Method run() throws Exception
                {
                    Class cl = Class.forName("java.lang.ClassLoader");

                    Method result = cl.getDeclaredMethod(methodName, parameterTypes);

                    // Just make it accessible; no particular reason to make it unaccessible again.

                    result.setAccessible(true);

                    return result;
                }
            });
        }
        catch (PrivilegedActionException ex)
        {
            throw new RuntimeException(String.format("Unable to initialize ClassFactoryClassPool: %s",
                                                     InternalUtils.toMessage(ex)), ex);
        }
    }

    /**
     * Used to identify which class loaders have already been integrated into the pool.
     */
    private final Set<ClassLoader> allLoaders = CollectionFactory.newSet();

    private final Map<ClassLoader, ClassPath> leafLoaders = CollectionFactory.newMap();

    public ClassFactoryClassPool(ClassLoader contextClassLoader)
    {
        super(null);

        addClassLoaderIfNeeded(contextClassLoader);
    }

    /**
     * Returns the nearest super-class of the provided class that can be converted to a {@link CtClass}. This is used to
     * filter out Hibernate-style proxies (created as subclasses of oridnary classes). This will automatically add the
     * class' classLoader to the pool's class path.
     *
     * @param clazz class to import
     * @return clazz, or a super-class of clazz
     */
    public Class importClass(Class clazz)
    {
        addClassLoaderIfNeeded(clazz.getClassLoader());

        while (true)
        {
            try
            {
                String name = ClassFabUtils.toJavaClassName(clazz);

                get(name);

                break;
            }
            catch (NotFoundException ex)
            {
                clazz = clazz.getSuperclass();
            }
        }

        return clazz;
    }

    /**
     * Convienience method for adding to the ClassPath for a particular class loader.
     * <p/>
     *
     * @param loader the class loader to add (derived from a loaded class, and may be null for some system classes)
     */
    public synchronized void addClassLoaderIfNeeded(ClassLoader loader)
    {
        Set<ClassLoader> leaves = leafLoaders.keySet();
        if (loader == null || leaves.contains(loader) || allLoaders.contains(loader)) return;

        // Work out if this loader is a child of a loader we have already.
        ClassLoader existingLeaf = loader;
        while (existingLeaf != null && !leaves.contains(existingLeaf))
        {
            existingLeaf = existingLeaf.getParent();
        }

        if (existingLeaf != null)
        {
            // The new loader is a child of an existing leaf.
            // So we remove the old leaf before we add the new loader
            ClassPath priorPath = leafLoaders.get(existingLeaf);
            removeClassPath(priorPath);
            leafLoaders.remove(existingLeaf);
        }

        ClassPath path = new LoaderClassPath(loader);
        leafLoaders.put(loader, path);
        insertClassPath(path);

        ClassLoader l = loader;
        while (l != null)
        {
            allLoaders.add(l);
            l = l.getParent();
        }
    }

    /**
     * Overriden to remove a deadlock producing synchronized block. We expect that the defineClass() methods will have
     * been marked as accessible statically (by this class), so there's no need to set them accessible again.
     */
    @Override
    public Class toClass(CtClass ct, ClassLoader loader, ProtectionDomain domain)
            throws CannotCompileException
    {
        Throwable failure;

        try
        {
            byte[] b = ct.toBytecode();

            boolean hasDomain = domain != null;

            Method method = hasDomain ? defineClassWithProtectionDomain : defineClass;

            Object[] args = hasDomain
                            ? new Object[] {ct.getName(), b, 0, b.length, domain}
                            : new Object[] {ct.getName(), b, 0, b.length};

            return (Class) method.invoke(loader, args);
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        throw new CannotCompileException(
                String.format("Failure defining new class %s: %s",
                              ct.getName(),
                              InternalUtils.toMessage(failure)), failure);
    }
}
