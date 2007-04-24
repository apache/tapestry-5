// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

import java.security.ProtectionDomain;

import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.tapestry.ioc.services.ClassFabUtils;

/**
 * Wrapper around Javassist's {@link javassist.ClassPool} that manages the creation of new instances
 * of {@link javassist.CtClass} and converts finished CtClass's into instantiable Classes.
 * 
 * @author Howard Lewis Ship
 */
class CtClassSource
{
    private final ClassFactoryClassPool _pool;

    private final ClassLoader _loader;

    private final ProtectionDomain _domain = getClass().getProtectionDomain();

    private int _createdClassCount = 0;

    /**
     * Returns the number of classes (and interfaces) created by this source.
     */
    public int getCreatedClassCount()
    {
        return _createdClassCount;
    }

    public CtClassSource(ClassFactoryClassPool pool, ClassLoader loader)
    {
        _pool = pool;
        _loader = loader;
    }

    public CtClass getCtClass(Class searchClass)
    {
        ClassLoader loader = searchClass.getClassLoader();

        // Add the class loader for the searchClass to the class pool and
        // delegating class loader if needed.

        _pool.addClassLoaderIfNeeded(loader);

        String name = ClassFabUtils.getJavaClassName(searchClass);

        try
        {
            return _pool.get(name);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ServiceMessages.unableToLookupClass(name, ex), ex);
        }
    }

    public synchronized CtClass newClass(String name, Class superClass)
    {
        CtClass ctSuperClass = getCtClass(superClass);

        return _pool.makeClass(name, ctSuperClass);
    }

    private static final String WRITE_DIR = System.getProperty("javassist-write-dir");

    public synchronized Class createClass(CtClass ctClass)
    {
        if (WRITE_DIR != null) writeClass(ctClass);

        try
        {
            Class result = _pool.toClass(ctClass, _loader, _domain);

            _createdClassCount++;

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
            boolean pruning = ctClass.stopPruning(true);

            ctClass.writeFile(WRITE_DIR);

            ctClass.defrost();

            ctClass.stopPruning(pruning);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}