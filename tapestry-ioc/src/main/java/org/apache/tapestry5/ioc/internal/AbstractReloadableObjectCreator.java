// Copyright 2010 The Apache Software Foundation
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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.Translator;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.services.UpdateListener;
import org.slf4j.Logger;

@SuppressWarnings("all")
public abstract class AbstractReloadableObjectCreator implements ObjectCreator, UpdateListener, Translator
{
    private class XLoader extends Loader
    {
        public XLoader(ClassLoader parent, ClassPool pool)
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

    private final String packageName;

    private final String classFilePath;

    private final Logger logger;

    private final OperationTracker tracker;

    private Object instance;

    private File classFile;

    private long lastModifiedTimestamp = 0;

    private boolean firstTime = true;

    private final Set<String> classesToLoad = CollectionFactory.newSet();

    protected AbstractReloadableObjectCreator(ClassLoader baseClassLoader, String implementationClassName,
            Logger logger, OperationTracker tracker)
    {
        this.baseClassLoader = baseClassLoader;
        this.implementationClassName = implementationClassName;
        this.logger = logger;
        this.tracker = tracker;

        packageName = toPackageName(implementationClassName);

        classFilePath = ClassFabUtils.getPathForClassNamed(implementationClassName);

    }

    private String toPackageName(String name)
    {
        int dotx = name.lastIndexOf('.');

        return dotx < 0 ? "" : name.substring(0, dotx);
    }

    public synchronized void checkForUpdates()
    {
        if (instance == null)
            return;

        if (classFile.lastModified() == lastModifiedTimestamp)
            return;

        if (logger.isDebugEnabled())
            logger.debug(String.format("Implementation class %s has changed and will be reloaded on next use.",
                    implementationClassName));

        instance = null;
        classFile = null;
        lastModifiedTimestamp = 0;
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
                updateTrackingInfo();

                Class reloadedClass = reloadImplementationClass();

                return createInstance(reloadedClass);
            };
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

        Loader loader = new XLoader(threadDeadlockBuffer, pool);

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
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(String.format("Unable to %s class %s: %s", firstTime ? "load" : "reload",
                    implementationClassName, InternalUtils.toMessage(ex)), ex);
        }
    }

    private URL getURLForClass(String className) throws ClassNotFoundException
    {
        String path = ClassFabUtils.getPathForClassNamed(className);

        URL result = baseClassLoader.getResource(path);

        if (result == null)
            throw new ClassNotFoundException(String.format("Unable to locate URL for class %s.", className));

        return result;
    }

    private void updateTrackingInfo()
    {
        URL url = baseClassLoader.getResource(classFilePath);

        if (url == null)
            throw new RuntimeException(String.format(
                    "Unable to reload class %s as it has been deleted. You may need to restart the application.",
                    implementationClassName));

        classFile = ClassFabUtils.toFileFromFileProtocolURL(url);

        lastModifiedTimestamp = classFile.lastModified();
    }

    public long getLastModifiedTimestamp()
    {
        return lastModifiedTimestamp;
    }

    private boolean shouldLoadClassNamed(String name)
    {
        return classesToLoad.contains(name);
    }

    private void add(String className)
    {
        if (classesToLoad.contains(className))
            return;

        // System.err.printf("Adding %s\n", className);
        logger.debug(String.format("Marking class %s to be (re-)loaded", className));

        classesToLoad.add(className);
    }

    public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException
    {
        logger.debug(String.format("BEGIN Analyzing %s", className));

        CtClass ctClass = pool.get(className);

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

            public void edit(FieldAccess f) throws CannotCompileException
            {

            }

            public void edit(MethodCall m) throws CannotCompileException
            {
                // String invokedMethodClassName = m.getClassName();
                //
                // if (classesToLoad.contains(invokedMethodClassName))
                // return;
                //
                // try
                // {
                // CtMethod method = m.getMethod();
                //
                // if (!Modifier.isPublic(method.getModifiers()))
                // return;
                //
                // add(invokedMethodClassName);
                // }
                // catch (NotFoundException ex)
                // {
                // throw new RuntimeException(ex);
                // }
            }

            public void edit(NewExpr e) throws CannotCompileException
            {
                String newInstanceClassName = e.getClassName();

                if (classesToLoad.contains(newInstanceClassName))
                    return;

                if (isInnerClass(newInstanceClassName))
                    add(newInstanceClassName);
            }

        });

        logger.debug(String.format("  END Analyzing %s", className));
    }

    /** Is the class an inner class of some other class already marked to be loaded by the special class loader? */
    private boolean isInnerClass(String className)
    {
        int dollarx = className.indexOf("$");

        return dollarx < 0 ? false : classesToLoad.contains(className.substring(0, dollarx));
    }

    public void start(ClassPool pool) throws NotFoundException, CannotCompileException
    {

    }

}
