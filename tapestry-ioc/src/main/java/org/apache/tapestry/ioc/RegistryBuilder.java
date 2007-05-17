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

package org.apache.tapestry.ioc;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.annotations.SubModule;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.internal.DefaultModuleDefImpl;
import org.apache.tapestry.ioc.internal.LogSourceImpl;
import org.apache.tapestry.ioc.internal.RegistryImpl;
import org.apache.tapestry.ioc.internal.RegistryWrapper;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.TapestryIOCModule;

/**
 * Used to construct the IoC {@link org.apache.tapestry.ioc.Registry}. This class is <em>not</em>
 * threadsafe. The Registry, once created, <em>is</em> threadsafe.
 */
public final class RegistryBuilder
{
    private final OneShotLock _lock = new OneShotLock();

    /** Module defs, keyed on module id. */
    final List<ModuleDef> _modules = newList();

    private final ClassLoader _classLoader;

    private final Log _log;

    private final LogSource _logSource;

    private final ClassFactory _classFactory;

    public RegistryBuilder()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public RegistryBuilder(ClassLoader classLoader)
    {
        this(classLoader, new LogSourceImpl());
    }

    public RegistryBuilder(ClassLoader classLoader, LogSource logSource)
    {
        _classLoader = classLoader;
        _logSource = logSource;
        _log = logSource.getLog(RegistryBuilder.class);

        // Make the ClassFactory appear to be a service inside TapestryIOCModule, even before that
        // module exists.

        Log classFactoryLog = logSource.getLog(TapestryIOCModule.class.getName() + ".ClassFactory");

        _classFactory = new ClassFactoryImpl(_classLoader, classFactoryLog);

        add(TapestryIOCModule.class);
    }

    public void add(ModuleDef moduleDef)
    {
        _lock.check();

        // TODO: Some way to ensure that duplicate modules are not being added.

        _modules.add(moduleDef);
    }

    public void add(Class... moduleBuilderClasses)
    {
        _lock.check();

        List<Class> queue = newList(Arrays.asList(moduleBuilderClasses));

        while (!queue.isEmpty())
        {
            Class c = queue.remove(0);

            ModuleDef def = new DefaultModuleDefImpl(c, _log, _classFactory);
            add(def);

            SubModule annotation = ((AnnotatedElement) c).getAnnotation(SubModule.class);

            if (annotation == null) continue;

            for (Class sub : annotation.value())
                queue.add(sub);
        }
    }

    public void add(String classname)
    {
        _lock.check();

        try
        {
            Class builderClass = Class.forName(classname, true, _classLoader);

            add(builderClass);
        }
        catch (ClassNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public Registry build()
    {
        _lock.lock();

        RegistryImpl registry = new RegistryImpl(_modules, _classFactory, _logSource);

        registry.eagerLoadServices();

        return new RegistryWrapper(registry);
    }

    public ClassLoader getClassLoader()
    {
        _lock.check();

        return _classLoader;
    }

    public Log getLog()
    {
        _lock.check();

        return _log;
    }
}
