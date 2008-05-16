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

package org.apache.tapestry.ioc;

import org.apache.tapestry.ioc.annotation.SubModule;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.internal.DefaultModuleDefImpl;
import org.apache.tapestry.ioc.internal.LoggerSourceImpl;
import org.apache.tapestry.ioc.internal.RegistryImpl;
import org.apache.tapestry.ioc.internal.RegistryWrapper;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.TapestryIOCModule;
import org.slf4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Used to construct the IoC {@link org.apache.tapestry.ioc.Registry}. This class is <em>not</em> thread-safe. The
 * Registry, once created, <em>is</em> thread-safe.
 */
public final class RegistryBuilder
{
    private final OneShotLock lock = new OneShotLock();

    /**
     * Module defs, keyed on module id.
     */
    final List<ModuleDef> modules = CollectionFactory.newList();

    private final ClassLoader classLoader;

    private final Logger logger;

    private final LoggerSource loggerSource;

    private final ClassFactory classFactory;

    private final Set<Class> addedModuleClasses = CollectionFactory.newSet();

    public RegistryBuilder()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public RegistryBuilder(ClassLoader classLoader)
    {
        this(classLoader, new LoggerSourceImpl());
    }

    public RegistryBuilder(ClassLoader classLoader, LoggerSource loggerSource)
    {
        this.classLoader = classLoader;
        this.loggerSource = loggerSource;
        logger = loggerSource.getLogger(RegistryBuilder.class);

        // Make the ClassFactory appear to be a service inside TapestryIOCModule, even before that
        // module exists.

        Logger classFactoryLogger = loggerSource.getLogger(TapestryIOCModule.class.getName() + ".ClassFactory");

        classFactory = new ClassFactoryImpl(this.classLoader, classFactoryLogger);

        add(TapestryIOCModule.class);
    }

    /**
     * Adds a {@link ModuleDef} to the registry, returning the builder for further configuration.
     */
    public RegistryBuilder add(ModuleDef moduleDef)
    {
        lock.check();

        // TODO: Some way to ensure that duplicate modules are not being added.
        // Part of TAPESTRY-2117 is in add(Class...) and that may be as much as we can
        // do as there is no concept of ModuleDef identity.

        modules.add(moduleDef);

        return this;
    }

    /**
     * Adds a number of modules (as module classes) to the registry, returning the builder for further configuration.
     *
     * @see org.apache.tapestry.ioc.annotation.SubModule
     */
    public RegistryBuilder add(Class... moduleBuilderClasses)
    {
        lock.check();

        List<Class> queue = CollectionFactory.newList(Arrays.asList(moduleBuilderClasses));

        while (!queue.isEmpty())
        {
            Class c = queue.remove(0);

            // Quietly ignore previously added classes.

            if (addedModuleClasses.contains(c)) continue;

            addedModuleClasses.add(c);

            ModuleDef def = new DefaultModuleDefImpl(c, logger, classFactory);
            add(def);

            SubModule annotation = ((AnnotatedElement) c).getAnnotation(SubModule.class);

            if (annotation == null) continue;

            queue.addAll(Arrays.asList(annotation.value()));
        }

        return this;
    }

    /**
     * Adds a number of module classes (specified by fully qualified class name) to the registry, returning the builder
     * for further configuration.
     *
     * @see org.apache.tapestry.ioc.annotation.SubModule
     */
    public RegistryBuilder add(String classname)
    {
        lock.check();

        try
        {
            Class builderClass = Class.forName(classname, true, classLoader);

            add(builderClass);
        }
        catch (ClassNotFoundException ex)
        {
            throw new IllegalArgumentException(ex);
        }

        return this;
    }

    /**
     * Constructs and returns the registry; this may only be done once. The caller is responsible for invoking {@link
     * org.apache.tapestry.ioc.Registry#performRegistryStartup()}.
     */
    public Registry build()
    {
        lock.lock();

        RegistryImpl registry = new RegistryImpl(modules, classFactory, loggerSource);

        return new RegistryWrapper(registry);
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public Logger getLogger()
    {
        return logger;
    }
}
