// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import javassist.*;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.events.UpdateListener;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.internal.services.CtClassSourceImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper around a Javassist class loader that allows certain classes to be modified as they are loaded.
 */
public final class ComponentInstantiatorSourceImpl extends InvalidationEventHubImpl implements Translator, ComponentInstantiatorSource, UpdateListener
{
    /**
     * Add -Djavassist-write-dir=target/transformed-classes to the command line to force output of transformed classes
     * to disk (for hardcore debugging).
     */
    private static final String JAVASSIST_WRITE_DIR = System.getProperty("javassist-write-dir");

    private final Set<String> controlledPackageNames = CollectionFactory.newSet();

    private final URLChangeTracker changeTracker = new URLChangeTracker();

    private final ClassLoader parent;

    private final InternalRequestGlobals internalRequestGlobals;

    private Loader loader;

    private final ComponentClassTransformer transformer;

    private final Logger logger;

    private ClassFactory classFactory;

    /**
     * Map from class name to Instantiator.
     */
    private final Map<String, Instantiator> classNameToInstantiator = CollectionFactory.newMap();

    private CtClassSource classSource;

    private class PackageAwareLoader extends Loader
    {
        public PackageAwareLoader(ClassLoader parent, ClassPool classPool)
        {
            super(parent, classPool);
        }


        /**
         * Synchronizes on the parent class loader before continuing, which is necessary to prevent thread deadlocks. Any classes
         * loaded, or transformed, by this class loader will do so with the parent (context) class loader locked.
         * The required order is always that the context class loader be locked, then the child class loader.  Painful.
         */
        @Override
        protected Class loadClass(String name, boolean resolve) throws ClassFormatError, ClassNotFoundException
        {
            synchronized (getParent())
            {
                return super.loadClass(name, resolve);
            }
        }

        /**
         * Determines if the class name represents a component class from a controlled package.  If so,
         * super.findClass() will load it and transform it. Returns null if not in a controlled package, allowing the
         * parent class loader to do the work.
         *
         * @param className
         * @return the loaded transformed Class, or null to force a load of the class from the parent class loader
         * @throws ClassNotFoundException
         */
        @Override
        protected Class findClass(String className) throws ClassNotFoundException
        {
            if (inControlledPackage(className))
            {
                // TAPESTRY-2561: Prevent other threads from creating new classes in either
                // the component class loader or in the context class loader (which is used for
                // IoC proxies and the like). This is draconian, but the deadlock issue remains.                
                //  synchronized (InternalConstants.GLOBAL_CLASS_CREATION_MUTEX)
                // {
                return super.findClass(className);
                // }
            }

            // Returning null forces delegation to the parent class loader.

            return null;
        }
    }

    public ComponentInstantiatorSourceImpl(Logger logger, ClassLoader parent, ComponentClassTransformer transformer,
                                           InternalRequestGlobals internalRequestGlobals)
    {
        this.parent = parent;
        this.transformer = transformer;
        this.logger = logger;
        this.internalRequestGlobals = internalRequestGlobals;

        initializeService();
    }

    public synchronized void checkForUpdates()
    {
        if (!changeTracker.containsChanges()) return;

        changeTracker.clear();
        classNameToInstantiator.clear();

        // Release the existing class pool, loader and so forth.
        // Create a new one.

        initializeService();

        // Tell everyone that the world has changed and they should discard
        // their cache.

        fireInvalidationEvent();
    }

    /**
     * Invoked at object creation, or when there are updates to class files (i.e., invalidation), to create a new set of
     * Javassist class pools and loaders.
     */
    private void initializeService()
    {
        ClassFactoryClassPool classPool = new ClassFactoryClassPool(parent);

        loader = new PackageAwareLoader(parent, classPool);

        ClassPath path = new LoaderClassPath(loader);

        classPool.appendClassPath(path);

        classSource = new CtClassSourceImpl(classPool, loader);

        try
        {
            loader.addTranslator(classPool, this);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        classFactory = new ClassFactoryImpl(loader, classPool, classSource, logger);
    }

    // This is called from well within a synchronized block.    The component layer class loader,
    // and the context class loader, should each be locked.

    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException
    {
        logger.debug("BEGIN onLoad " + classname);

        // This is our chance to make changes to the CtClass before it is loaded into memory.

        String diag = "FAIL";

        // If we are loading a class, it is because it is in a controlled package. There may be
        // errors in the class that keep it from loading. By adding it to the change tracker
        // early, we ensure that when the class is fixed, the change is picked up. Originally,
        // this code was at the end of the method, and classes that contained errors would not be
        // reloaded even after the code was fixed.

        addClassFileToChangeTracker(classname);

        try
        {
            CtClass ctClass = pool.get(classname);

            // Force the creation of the super-class before the target class.

            forceSuperclassTransform(ctClass);

            // Do the transformations here

            transformer.transformComponentClass(ctClass, loader);

            writeClassToFileSystemForHardCoreDebuggingPurposesOnly(ctClass);

            diag = "END";
        }
        catch (RuntimeException classLoaderException)
        {
            internalRequestGlobals.storeClassLoaderException(classLoaderException);

            throw classLoaderException;
        }
        finally
        {
            logger.debug(String.format("%5s onLoad %s", diag, classname));
        }
    }

    private void writeClassToFileSystemForHardCoreDebuggingPurposesOnly(CtClass ctClass)
    {
        if (JAVASSIST_WRITE_DIR == null) return;

        try
        {
            boolean p = ctClass.stopPruning(true);
            ctClass.writeFile(JAVASSIST_WRITE_DIR);
            ctClass.defrost();
            ctClass.stopPruning(p);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void addClassFileToChangeTracker(String classname)
    {
        String path = classname.replace('.', '/') + ".class";

        URL url = loader.getResource(path);

        changeTracker.add(url);
    }

    private void forceSuperclassTransform(CtClass ctClass) throws NotFoundException
    {
        CtClass superClass = ctClass.getSuperclass();

        findClass(superClass.getName());
    }

    /**
     * Does nothing.
     */
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException
    {
    }

    public synchronized Instantiator findInstantiator(String className)
    {
        Instantiator result = classNameToInstantiator.get(className);

        if (result == null)
        {
            // Force the creation of the class (and the transformation of the class).

            findClass(className);

            result = transformer.createInstantiator(className);

            classNameToInstantiator.put(className, result);
        }

        return result;
    }

    private Class findClass(String classname)
    {
        try
        {
            return loader.loadClass(classname);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns true if the package for the class name is in a package that is controlled by the enhancer. Controlled
     * packages are identified by {@link #addPackage(String)}.
     */

    boolean inControlledPackage(String classname)
    {
        String packageName = stripTail(classname);

        while (packageName != null)
        {
            if (controlledPackageNames.contains(packageName)) return true;

            packageName = stripTail(packageName);
        }

        return false;
    }

    private String stripTail(String input)
    {
        int lastdot = input.lastIndexOf('.');

        if (lastdot < 0) return null;

        return input.substring(0, lastdot);
    }

    // synchronized may be overkill, but that's ok.
    public synchronized void addPackage(String packageName)
    {
        Defense.notBlank(packageName, "packageName");

        // TODO: Should we check that packages are not nested?

        controlledPackageNames.add(packageName);
    }

    public boolean exists(String className)
    {
        String path = className.replace(".", "/") + ".class";

        return parent.getResource(path) != null;
    }

    public ClassFactory getClassFactory()
    {
        return classFactory;
    }

    public CtClassSource getClassSource()
    {
        return classSource;
    }
}
