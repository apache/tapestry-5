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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.Translator;

import org.apache.tapestry.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry.internal.events.UpdateListener;
import org.apache.tapestry.internal.util.URLChangeTracker;
import org.apache.tapestry.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.slf4j.Logger;

/**
 * A wrapper around a Javassist class loader that allows certain classes to be modified as they are
 * loaded.
 */
public final class ComponentInstantiatorSourceImpl extends InvalidationEventHubImpl implements
        Translator, ComponentInstantiatorSource, UpdateListener
{
    /**
     * Add -Djavassist-write-dir=target/transformed-classes to the command line to force output of
     * transformed classes to disk (for hardcore debugging).
     */
    private static final String JAVASSIST_WRITE_DIR = System.getProperty("javassist-write-dir");

    private final Set<String> _controlledPackageNames = newSet();

    private final URLChangeTracker _changeTracker = new URLChangeTracker();

    private final ClassLoader _parent;

    private ClassFactoryClassPool _classPool;

    private Loader _loader;

    private final ComponentClassTransformer _transformer;

    private final Logger _logger;

    private ClassFactory _classFactory;

    /** Map from class name to Instantiator. */
    private final Map<String, Instantiator> _instantiatorMap = newMap();

    private class PackageAwareLoader extends Loader
    {
        public PackageAwareLoader(ClassLoader parent, ClassPool classPool)
        {
            super(parent, classPool);
        }

        @Override
        protected Class findClass(String className) throws ClassNotFoundException
        {
            if (inControlledPackage(className))
                return super.findClass(className);

            // Returning null forces delegation to the parent class loader.

            return null;
        }

    }

    public ComponentInstantiatorSourceImpl(ClassLoader parent,
            ComponentClassTransformer transformer, Logger logger)
    {
        _parent = parent;
        _transformer = transformer;
        _logger = logger;

        initializeService();
    }

    public synchronized void checkForUpdates()
    {
        if (!_changeTracker.containsChanges())
            return;

        _changeTracker.clear();
        _instantiatorMap.clear();

        // Release the existing class pool, loader and so forth.
        // Create a new one.

        initializeService();

        // Tell everyone that the world has changed and they should discard
        // their cache.

        fireInvalidationEvent();
    }

    /**
     * Invoked at object creation, or when there are updates to class files (i.e., invalidation), to
     * create a new set of Javassist class pools and loaders.
     */
    private void initializeService()
    {
        _classPool = new ClassFactoryClassPool(_parent);

        _loader = new PackageAwareLoader(_parent, _classPool);

        ClassPath path = new LoaderClassPath(_loader);

        _classPool.appendClassPath(path);

        try
        {
            _loader.addTranslator(_classPool, this);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        _classFactory = new ClassFactoryImpl(_loader, _classPool, _logger);
    }

    // This is called from well within a synchronized block.
    public void onLoad(ClassPool pool, String classname) throws NotFoundException,
            CannotCompileException
    {
        _logger.debug("BEGIN onLoad " + classname);

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

            _transformer.transformComponentClass(ctClass, _loader);

            writeClassToFileSystemForHardCoreDebuggingPurposesOnly(ctClass);

            diag = "END";
        }
        catch (ClassNotFoundException ex)
        {
            throw new CannotCompileException(ex);
        }
        finally
        {
            _logger.debug(String.format("%5s onLoad %s", diag, classname));
        }
    }

    private void writeClassToFileSystemForHardCoreDebuggingPurposesOnly(CtClass ctClass)
    {
        if (JAVASSIST_WRITE_DIR == null)
            return;

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

        URL url = _loader.getResource(path);

        _changeTracker.add(url);
    }

    private void forceSuperclassTransform(CtClass ctClass) throws NotFoundException,
            ClassNotFoundException
    {
        CtClass superClass = ctClass.getSuperclass();

        findClass(superClass.getName());
    }

    /** Does nothing. */
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException
    {
    }

    public synchronized Instantiator findInstantiator(String classname)
    {
        Instantiator result = _instantiatorMap.get(classname);

        if (result == null)
        {
            Class instanceClass = findClass(classname);

            result = _transformer.createInstantiator(instanceClass);

            _instantiatorMap.put(classname, result);
        }

        return result;
    }

    private Class findClass(String classname)
    {
        try
        {
            return _loader.loadClass(classname);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns true if the package for the class name is in a package that is controlled by the
     * enhancer. Controlled packages are identified by {@link #addPackage(String)}.
     */

    boolean inControlledPackage(String classname)
    {
        String packageName = stripTail(classname);

        while (packageName != null)
        {
            if (_controlledPackageNames.contains(packageName))
                return true;

            packageName = stripTail(packageName);
        }

        return false;
    }

    private String stripTail(String input)
    {
        int lastdot = input.lastIndexOf('.');

        if (lastdot < 0)
            return null;

        return input.substring(0, lastdot);
    }

    // synchronized may be overkill, but that's ok.
    public synchronized void addPackage(String packageName)
    {
        Defense.notBlank(packageName, "packageName");

        // TODO: Should we check that packages are not nested?

        _controlledPackageNames.add(packageName);
    }

    public boolean exists(String className)
    {
        String path = className.replace(".", "/") + ".class";

        return _parent.getResource(path) != null;
    }

    public ClassFactory getClassFactory()
    {
        return _classFactory;
    }
}
