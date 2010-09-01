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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.Translator;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.services.UpdateListener;
import org.slf4j.Logger;

@SuppressWarnings("all")
public abstract class AbstractReloadableObjectCreator implements ObjectCreator, UpdateListener, Translator
{
    private final ProtectionDomain domain = getClass().getProtectionDomain();

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

    private final String classFilePath;

    private final Logger logger;

    private final OperationTracker tracker;

    private Object instance;

    private File classFile;

    private long lastModifiedTimestamp = 0;

    private boolean firstTime = true;

    protected AbstractReloadableObjectCreator(ClassLoader baseClassLoader, String implementationClassName,
            Logger logger, OperationTracker tracker)
    {
        this.baseClassLoader = baseClassLoader;
        this.implementationClassName = implementationClassName;
        this.logger = logger;
        this.tracker = tracker;

        this.classFilePath = ClassFabUtils.getPathForClassNamed(implementationClassName);

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

        // For TAPESTRY-2561, we're introducing a class loader between the parent (i.e., the
        // context class loader), and the component class loader, to try and prevent the deadlocks
        // that we've been seeing.

        ClassLoader threadDeadlockBuffer = new URLClassLoader(new URL[0], baseClassLoader);

        Loader loader = new XLoader(threadDeadlockBuffer, pool);

        ClassPath path = new LoaderClassPath(loader);

        pool.appendClassPath(path);

        try
        {
            loader.addTranslator(pool, this);

            CtClass implCtClass = pool.get(implementationClassName);

            Class result = pool.toClass(implCtClass, loader, domain);

            firstTime = false;

            return result;
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(String.format("Unable to %s class %s: %s", firstTime ? "load" : "reload",
                    implementationClassName, InternalUtils.toMessage(ex)), ex);
        }
    }

    private byte[] readClassData(String name) throws ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[10000];

        URL url = getURLForClass(name);

        InputStream in = null;

        try
        {
            in = url.openStream();

            while (true)
            {
                int length = in.read(buffer);

                if (length < 0)
                    break;

                baos.write(buffer, 0, length);
            }

            in.close();

            in = null;
        }
        catch (IOException ex)
        {
            InternalUtils.close(in);

            throw new ClassNotFoundException(InternalUtils.toMessage(ex), ex);
        }

        return baos.toByteArray();
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
        return name.equals(implementationClassName) || name.startsWith(implementationClassName + "$");
    }

    public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException
    {

    }

    public void start(ClassPool pool) throws NotFoundException, CannotCompileException
    {

    }

}
