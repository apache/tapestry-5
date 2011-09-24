// Copyright 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal;

import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ReloadAware;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.services.UpdateListener;
import org.slf4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

@SuppressWarnings("all")
public abstract class AbstractReloadableObjectCreator implements ObjectCreator, UpdateListener, Translator
{
    private class InternalLoader extends Loader
    {
        public InternalLoader(ClassLoader parent, ClassPool pool)
        {
            super(parent, pool);
        }

        @Override
        protected Class findClass(String name) throws ClassNotFoundException
        {
            if (shouldLoadClassNamed(name))
                return super.findClass(name);

            return null; // Force delegation to parent class loader
        }
    }

    private final ClassLoader baseClassLoader;

    private final String implementationClassName;

    private final Logger logger;

    private final OperationTracker tracker;

    private final URLChangeTracker changeTracker = new URLChangeTracker();

    /**
     * The set of class names that should be loaded by the class loader. This is necessary to support
     * reloading the class when a base class changes, and to properly support access to protected methods.
     */
    private final Set<String> classesToLoad = CollectionFactory.newSet();

    private Object instance;

    private boolean firstTime = true;

    protected AbstractReloadableObjectCreator(ClassLoader baseClassLoader, String implementationClassName,
                                              Logger logger, OperationTracker tracker)
    {
        this.baseClassLoader = baseClassLoader;
        this.implementationClassName = implementationClassName;
        this.logger = logger;
        this.tracker = tracker;
    }

    public synchronized void checkForUpdates()
    {
        if (instance == null)
            return;

        if (!changeTracker.containsChanges())
            return;

        if (logger.isDebugEnabled())
            logger.debug(String.format("Implementation class %s has changed and will be reloaded on next use.",
                    implementationClassName));

        changeTracker.clear();

        boolean reloadNow = informInstanceOfReload();

        instance = reloadNow ? createInstance() : null;
    }

    private boolean informInstanceOfReload()
    {
        if (instance instanceof ReloadAware)
        {
            ReloadAware ra = (ReloadAware) instance;

            return ra.shutdownImplementationForReload();
        }

        return false;
    }

    public synchronized Object createObject()
    {
        if (instance == null)
            instance = createInstance();

        return instance;
    }

    private Object createInstance()
    {
        return tracker.invoke(String.format("Reloading class %s.", implementationClassName), new Invokable<Object>()
        {
            public Object invoke()
            {
                Class reloadedClass = reloadImplementationClass();

                return createInstance(reloadedClass);
            }

            ;
        });
    }

    /**
     * Invoked when an instance of the class is needed. It is the responsibility of this method (as implemented in a
     * subclass) to instantiate the class and inject dependencies into the class.
     *
     * @see InternalUtils#findAutobuildConstructor(Class)
     */
    abstract protected Object createInstance(Class clazz);

    private Class reloadImplementationClass()
    {
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s class %s.", firstTime ? "Loading" : "Reloading", implementationClassName));

        ClassFactoryClassPool pool = new ClassFactoryClassPool(baseClassLoader);

        ClassLoader threadDeadlockBuffer = new URLClassLoader(new URL[0], baseClassLoader);

        Loader loader = new InternalLoader(threadDeadlockBuffer, pool);

        ClassPath path = new LoaderClassPath(loader);

        pool.appendClassPath(path);

        classesToLoad.clear();
        add(implementationClassName);

        try
        {
            loader.addTranslator(pool, this);

            Class result = loader.loadClass(implementationClassName);

            firstTime = false;

            return result;
        } catch (Throwable ex)
        {
            throw new RuntimeException(String.format("Unable to %s class %s: %s", firstTime ? "load" : "reload",
                    implementationClassName, InternalUtils.toMessage(ex)), ex);
        }
    }

    private boolean shouldLoadClassNamed(String name)
    {
        return classesToLoad.contains(name);
    }

    private void add(String className)
    {
        if (classesToLoad.contains(className))
            return;

        logger.debug(String.format("Marking class %s to be (re-)loaded", className));

        classesToLoad.add(className);
    }

    public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException
    {
        logger.debug(String.format("BEGIN Analyzing %s", className));

        analyze(pool, className);

        trackClassFileChanges(className);

        logger.debug(String.format("  END Analyzing %s", className));
    }

    private void analyze(ClassPool pool, String className) throws NotFoundException, CannotCompileException
    {
        CtClass ctClass = pool.get(className);

        CtClass[] nestedClasses = ctClass.getNestedClasses();

        for (CtClass nc : nestedClasses)
        {
            add(nc.getName());
        }

        ctClass.instrument(new ExprEditor()
        {
            public void edit(ConstructorCall c) throws CannotCompileException
            {
                if (c.getMethodName().equals("this"))
                    return;

                String cn = c.getClassName();

                String classFilePath = ClassFabUtils.getPathForClassNamed(cn);

                URL url = baseClassLoader.getResource(classFilePath);

                // If the base class is also a file on the file system then mark
                // that it should be loaded by the same class loader. This serves two
                // purposes: first, if the base class is in the same package then
                // protected access will work properly. Secondly, if the base implementation
                // changes, the service implementation will be reloaded.

                if (url != null && url.getProtocol().equals("file"))
                    add(cn);
            }
        });
    }

    private void trackClassFileChanges(String className)
    {
        if (isInnerClassName(className))
            return;

        String path = ClassFabUtils.getPathForClassNamed(className);

        URL url = baseClassLoader.getResource(path);

        if (url != null && url.getProtocol().equals("file"))
            changeTracker.add(url);
    }

    private boolean isInnerClassName(String className)
    {
        return className.indexOf('$') >= 0;
    }

    /**
     * Does nothing.
     */
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException
    {

    }

}
