// Copyright 2010-2013 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.internal.plastic.ClassLoaderDelegate;
import org.apache.tapestry5.internal.plastic.PlasticClassLoader;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.internal.plastic.asm.ClassReader;
import org.apache.tapestry5.internal.plastic.asm.ClassVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ReloadAware;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

@SuppressWarnings("all")
public abstract class AbstractReloadableObjectCreator implements ObjectCreator, UpdateListener, ClassLoaderDelegate
{
    private final ClassLoader baseClassLoader;

    private final String implementationClassName;

    private final Logger logger;

    private final OperationTracker tracker;

    private final URLChangeTracker changeTracker = new URLChangeTracker();

    private final PlasticProxyFactory proxyFactory;

    /**
     * The set of class names that should be loaded by the class loader. This is necessary to support
     * reloading the class when a base class changes, and to properly support access to protected methods.
     */
    private final Set<String> classesToLoad = CollectionFactory.newSet();

    private Object instance;

    private boolean firstTime = true;

    private PlasticClassLoader loader;

    protected AbstractReloadableObjectCreator(PlasticProxyFactory proxyFactory, ClassLoader baseClassLoader, String implementationClassName,
                                              Logger logger, OperationTracker tracker)
    {
        this.proxyFactory = proxyFactory;
        this.baseClassLoader = baseClassLoader;
        this.implementationClassName = implementationClassName;
        this.logger = logger;
        this.tracker = tracker;
    }

    @Override
    public synchronized void checkForUpdates()
    {
        if (instance == null || !changeTracker.containsChanges())
        {
            return;
        }

        logger.debug("Implementation class {} has changed and will be reloaded on next use.",
                implementationClassName);

        changeTracker.clear();

        loader = null;

        proxyFactory.clearCache();

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

    @Override
    public synchronized Object createObject()
    {
        if (instance == null)
        {
            instance = createInstance();
        }

        return instance;
    }

    private Object createInstance()
    {
        return tracker.invoke(String.format("Reloading class %s.", implementationClassName), new Invokable<Object>()
        {
            @Override
            public Object invoke()
            {
                Class reloadedClass = reloadImplementationClass();

                return createInstance(reloadedClass);
            }
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
        {
            logger.debug("{} class {}.", firstTime ? "Loading" : "Reloading", implementationClassName);
        }

        loader = new PlasticClassLoader(baseClassLoader, this);

        classesToLoad.clear();

        add(implementationClassName);

        try
        {
            Class result = loader.loadClass(implementationClassName);

            firstTime = false;

            return result;
        } catch (Throwable ex)
        {
            throw new RuntimeException(String.format("Unable to %s class %s: %s", firstTime ? "load" : "reload",
                    implementationClassName, ExceptionUtils.toMessage(ex)), ex);
        }
    }

    private void add(String className)
    {
        if (!classesToLoad.contains(className))
        {
            logger.debug("Marking class {} to be (re-)loaded", className);

            classesToLoad.add(className);
        }
    }

    @Override
    public boolean shouldInterceptClassLoading(String className)
    {
        return classesToLoad.contains(className);
    }

    @Override
    public Class<?> loadAndTransformClass(String className) throws ClassNotFoundException
    {
        logger.debug("BEGIN Analyzing {}", className);

        Class<?> result;

        try
        {
            result = doClassLoad(className);
        } catch (IOException ex)
        {
            throw new ClassNotFoundException(String.format("Unable to analyze and load class %s: %s", className,
                    ExceptionUtils.toMessage(ex)), ex);
        }

        trackClassFileChanges(className);

        logger.debug("  END Analyzing {}", className);

        return result;
    }

    public Class<?> doClassLoad(String className) throws IOException
    {
        ClassVisitor analyzer = new ClassVisitor(Opcodes.ASM7)
        {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
            {
                String path = superName + ".class";

                URL url = baseClassLoader.getResource(path);

                if (isFileURL(url))
                {
                    add(PlasticInternalUtils.toClassName(superName));
                }
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access)
            {
                // Anonymous inner classes show the outerName as null. Nested classes show the outer name as
                // the internal name of the containing class.
                if (outerName == null || classesToLoad.contains(PlasticInternalUtils.toClassName(outerName)))
                {
                    add(PlasticInternalUtils.toClassName(name));
                }
            }
        };


        String path = PlasticInternalUtils.toClassPath(className);

        InputStream stream = baseClassLoader.getResourceAsStream(path);

        assert stream != null;

        ByteArrayOutputStream classBuffer = new ByteArrayOutputStream(5000);
        byte[] buffer = new byte[5000];

        while (true)
        {
            int length = stream.read(buffer);

            if (length < 0)
            {
                break;
            }

            classBuffer.write(buffer, 0, length);
        }

        stream.close();

        byte[] bytecode = classBuffer.toByteArray();

        new ClassReader(new ByteArrayInputStream(bytecode)).accept(analyzer,
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);


        return loader.defineClassWithBytecode(className, bytecode);
    }

    private void trackClassFileChanges(String className)
    {
        if (isInnerClassName(className))
        {
            return;
        }

        String path = PlasticInternalUtils.toClassPath(className);

        URL url = baseClassLoader.getResource(path);

        if (isFileURL(url))
        {
            changeTracker.add(url);
        }
    }

    /**
     * Returns true if the url is non-null, and is for the "file:" protocol.
     */
    private boolean isFileURL(URL url)
    {
        return url != null && url.getProtocol().equals("file");
    }

    private boolean isInnerClassName(String className)
    {
        return className.indexOf('$') >= 0;
    }
}
