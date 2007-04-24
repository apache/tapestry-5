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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.Set;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * Used to ensure that {@link javassist.ClassPool#appendClassPath(javassist.ClassPath)} is invoked
 * within a synchronized lock, and also handles tricky class loading issues (caused by the creation
 * of classes, and class loaders, at runtime).
 * 
 * @author Howard Lewis Ship
 */
public class ClassFactoryClassPool extends ClassPool
{
    private ClassLoader _loader;

    private ClassPath _priorClassPath;

    /**
     * Used to identify which class loaders have already been integrated into the pool.
     */
    private Set<ClassLoader> _loaders = newSet();

    public ClassFactoryClassPool(ClassLoader contextClassLoader)
    {
        super(null);

        addClassLoaderIfNeeded(contextClassLoader);
    }

    /**
     * Convienience method for adding to the ClassPath for a particular class loader.
     * <p>
     * TODO: This code assumes that ClassLoaders are structured as a "line" not a proper "tree".
     * That is, if the ClassLoader hiearchy actually does have branches, rather than a straight line
     * from root to leaf, it may not work.
     * 
     * @param loader
     *            the class loader to add (derived from a loaded class, and may be null for some
     *            system classes)
     */
    public synchronized void addClassLoaderIfNeeded(ClassLoader loader)
    {
        if (loader == null || loader == _loader || _loaders.contains(loader)) return;

        ClassPath path = new LoaderClassPath(loader);

        if (_priorClassPath != null) removeClassPath(_priorClassPath);

        insertClassPath(path);

        _priorClassPath = path;

        ClassLoader l = loader;
        while (l != null)
        {
            _loaders.add(l);
            l = l.getParent();
        }

        _loader = loader;
    }

    public ClassLoader getLoader()
    {
        return _loader;
    }
}