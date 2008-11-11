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

package org.apache.tapestry5.ioc.internal.services;

import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.tapestry5.ioc.services.ClassFabUtils;

import java.security.ProtectionDomain;

/**
 * Wrapper around Javassist's {@link javassist.ClassPool} that manages the creation of new instances of {@link
 * javassist.CtClass} and converts finished CtClass's into instantiable Classes.
 */
public class CtClassSourceImpl implements CtClassSource
{
    private static final String WRITE_DIR = System.getProperty("javassist-write-dir");

    private final ClassFactoryClassPool pool;

    private final ClassLoader loader;

    private final ProtectionDomain domain = getClass().getProtectionDomain();

    private int createdClassCount = 0;

    /**
     * Returns the number of classes (and interfaces) created by this source.
     */
    public synchronized int getCreatedClassCount()
    {
        return createdClassCount;
    }

    public CtClassSourceImpl(ClassFactoryClassPool pool, ClassLoader loader)
    {
        this.pool = pool;
        this.loader = loader;
    }

    public synchronized CtClass toCtClass(Class searchClass)
    {
        ClassLoader loader = searchClass.getClassLoader();

        // Add the class loader for the searchClass to the class pool and
        // delegating class loader if needed.

        pool.addClassLoaderIfNeeded(loader);

        String name = ClassFabUtils.toJavaClassName(searchClass);

        return toCtClass(name);
    }

    public CtClass toCtClass(String name)
    {
        try
        {
            return pool.get(name);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ServiceMessages.unableToLookupClass(name, ex), ex);
        }
    }

    public CtClass newClass(String name, Class superClass)
    {
        CtClass ctSuperClass = toCtClass(superClass);

        return pool.makeClass(name, ctSuperClass);
    }


    public Class createClass(CtClass ctClass)
    {
        if (WRITE_DIR != null) writeClass(ctClass);

        try
        {
            Class result = pool.toClass(ctClass, loader, domain);

            synchronized (this)
            {
                createdClassCount++;
            }

            return result;
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(ServiceMessages.unableToWriteClass(ctClass, ex), ex);
        }
    }

    private void writeClass(CtClass ctClass)
    {
        try
        {
            ctClass.debugWriteFile(WRITE_DIR);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}
