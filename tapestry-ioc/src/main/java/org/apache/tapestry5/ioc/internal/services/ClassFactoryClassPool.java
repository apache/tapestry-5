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

package org.apache.tapestry.ioc.internal.services;

import javassist.*;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;
import org.apache.tapestry.ioc.services.ClassFabUtils;

import java.util.Map;
import java.util.Set;

/**
 * Used to ensure that {@link javassist.ClassPool#appendClassPath(javassist.ClassPath)} is invoked within a synchronized
 * lock, and also handles tricky class loading issues (caused by the creation of classes, and class loaders, at
 * runtime).
 */
public class ClassFactoryClassPool extends ClassPool
{
    /**
     * Used to identify which class loaders have already been integrated into the pool.
     */
    private final Set<ClassLoader> allLoaders = newSet();

    private final Map<ClassLoader, ClassPath> leafLoaders = newMap();

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
}