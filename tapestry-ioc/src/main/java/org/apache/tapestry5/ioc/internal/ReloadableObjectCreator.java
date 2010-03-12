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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.ProtectionDomain;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.services.UpdateListener;
import org.slf4j.Logger;

/**
 * Returns an {@link ObjectCreator} for lazily instantiation a given implementation class (with dependencies).
 * Once an instance is instantiated, it is cached ... until the underlying .class file changes, at which point
 * the class is reloaded and a new instance instantiated.
 */
public class ReloadableObjectCreator implements ObjectCreator, UpdateListener
{
    private class ReloadingClassLoader extends ClassLoader
    {
        private ReloadingClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException
        {
            if (isReloadingClass(name))
            {
                byte[] classData = readClassData(name);

                return defineClass(name, classData, 0, classData.length);
            }

            return super.loadClass(name);
        }

        private boolean isReloadingClass(String name)
        {
            // This class loader exists to reload the implementation class and any inner classes of the
            // implementation class.
            return name.equals(implementationClassName) || name.startsWith(implementationClassName + "$");
        }
    }

    private final ServiceBuilderResources resources;

    private final ClassLoader baseClassLoader;

    private final String implementationClassName;

    private final ProtectionDomain protectionDomain;

    private final String classFilePath;

    private final Logger logger;

    private Object instance;

    private File classFile;

    private long lastModifiedTimestamp = 0;

    private boolean firstTime = true;

    public ReloadableObjectCreator(ServiceBuilderResources resources, ClassLoader baseClassLoader,
            String implementationClassName, ProtectionDomain protectionDomain)
    {
        this.resources = resources;
        this.baseClassLoader = baseClassLoader;
        this.implementationClassName = implementationClassName;
        this.protectionDomain = protectionDomain;

        this.classFilePath = ClassFabUtils.getPathForClassNamed(implementationClassName);

        logger = resources.getLogger();
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
        updateTrackingInfo();

        Class reloadedClass = reloadImplementationClass();

        final Constructor constructor = InternalUtils.findAutobuildConstructor(reloadedClass);

        if (constructor == null)
            throw new RuntimeException(String.format(
                    "Service implementation class %s does not have a suitable public constructor.",
                    implementationClassName));

        ObjectCreator constructorServiceCreator = new ConstructorServiceCreator(resources, String.format(
                "%s (last modified %tc)", constructor, lastModifiedTimestamp), constructor);

        return constructorServiceCreator.createObject();
    }

    private Class reloadImplementationClass()
    {
        if (logger.isDebugEnabled())
            logger.debug("%s class %s.", firstTime ? "Loading" : "Reloading", implementationClassName);

        ClassLoader reloadingClassLoader = new ReloadingClassLoader(baseClassLoader);

        try
        {
            Class result = reloadingClassLoader.loadClass(implementationClassName);

            firstTime = false;

            return result;
        }
        catch (ClassNotFoundException ex)
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

        classFile = ClassFabUtils.toFileFromFileProtocolURL(url);

        lastModifiedTimestamp = classFile.lastModified();
    }

}
