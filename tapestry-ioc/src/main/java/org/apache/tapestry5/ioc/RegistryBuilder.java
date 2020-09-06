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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.commons.internal.*;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ModuleDef2;
import org.apache.tapestry5.ioc.internal.DefaultModuleDefImpl;
import org.apache.tapestry5.ioc.internal.LoggerSourceImpl;
import org.apache.tapestry5.ioc.internal.PerThreadOperationTracker;
import org.apache.tapestry5.ioc.internal.RegistryImpl;
import org.apache.tapestry5.ioc.internal.RegistryWrapper;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.modules.TapestryIOCModule;
import org.slf4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Used to construct the IoC {@link org.apache.tapestry5.ioc.Registry}. This class is <em>not</em> thread-safe. The
 * Registry, once created, <em>is</em> thread-safe.
 */
public final class RegistryBuilder
{
    private final OneShotLock lock = new OneShotLock();

    /**
     * Module defs, keyed on module id.
     */
    final List<ModuleDef2> modules = CollectionFactory.newList();

    private final ClassLoader classLoader;

    private final Logger logger;

    private final LoggerSource loggerSource;

    private final PlasticProxyFactory proxyFactory;

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

        // Make the Proxy Factory appear to be a service inside TapestryIOCModule, even before that
        // module exists.

        Logger proxyFactoryLogger = loggerSource.getLogger(TapestryIOCModule.class.getName() + ".PlasticProxyFactory");

        proxyFactory = new PlasticProxyFactoryImpl(this.classLoader, proxyFactoryLogger);

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

        modules.add(InternalUtils.toModuleDef2(moduleDef));

        return this;
    }

    /**
     * Adds a number of modules (as module classes) to the registry, returning the builder for further configuration.
     *
     * @see org.apache.tapestry5.ioc.annotations.ImportModule
     */
    public RegistryBuilder add(Class... moduleClasses)
    {
        lock.check();

        List<Class> queue = CollectionFactory.newList(Arrays.asList(moduleClasses));

        while (!queue.isEmpty())
        {
            Class c = queue.remove(0);

            // Quietly ignore previously added classes.

            if (addedModuleClasses.contains(c))
                continue;

            addedModuleClasses.add(c);

            logger.info("Adding module definition for " + c);

            ModuleDef def = new DefaultModuleDefImpl(c, logger, proxyFactory);
            add(def);


            @SuppressWarnings("RedundantCast")
            AnnotatedElement element = (AnnotatedElement) c;

            SubModule subModule = element.getAnnotation(SubModule.class);

            if (subModule != null)
            {
                queue.addAll(Arrays.asList(subModule.value()));
            }
            ImportModule importModule = element.getAnnotation(ImportModule.class);

            if (importModule != null)
            {
                queue.addAll(Arrays.asList(importModule.value()));
            }
        }

        return this;
    }

    /**
     * Adds a modle class (specified by fully qualified class name) to the registry, returning the builder
     * for further configuration.
     *
     * @see org.apache.tapestry5.ioc.annotations.ImportModule
     */
    public RegistryBuilder add(String classname)
    {
        lock.check();

        try
        {
            Class builderClass = Class.forName(classname, true, classLoader);

            add(builderClass);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Failure loading Tapestry IoC module class %s: %s", classname,
                    ExceptionUtils.toMessage(ex)), ex);
        }

        return this;
    }

    /**
     * Constructs and returns the registry; this may only be done once. The caller is responsible for invoking
     * {@link org.apache.tapestry5.ioc.Registry#performRegistryStartup()}.
     */
    public Registry build()
    {
        lock.lock();

        PerThreadOperationTracker tracker = new PerThreadOperationTracker(loggerSource.getLogger(Registry.class));

        RegistryImpl registry = new RegistryImpl(modules, proxyFactory, loggerSource, tracker);

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

    /**
     * Constructs the registry, adds a {@link ModuleDef} and a number of modules (as module classes) to the registry and
     * performs registry startup. The returned registry is ready to use. The caller is must not invoke
     * {@link org.apache.tapestry5.ioc.Registry#performRegistryStartup()}.
     *
     * @param moduleDef
     *         {@link ModuleDef} to add
     * @param moduleClasses
     *         modules (as module classes) to add
     * @return {@link Registry}
     * @since 5.2.0
     */
    public static Registry buildAndStartupRegistry(ModuleDef moduleDef, Class... moduleClasses)
    {
        RegistryBuilder builder = new RegistryBuilder();

        if (moduleDef != null)
            builder.add(moduleDef);

        builder.add(moduleClasses);

        Registry registry = builder.build();

        registry.performRegistryStartup();

        return registry;
    }

    /**
     * Constructs the registry, adds a number of modules (as module classes) to the registry and
     * performs registry startup. The returned registry is ready to use. The caller is must not invoke
     * {@link org.apache.tapestry5.ioc.Registry#performRegistryStartup()}.
     *
     * @param moduleClasses
     *         modules (as module classes) to add
     * @return {@link Registry}
     * @since 5.2.0
     */
    public static Registry buildAndStartupRegistry(Class... moduleClasses)
    {
        return buildAndStartupRegistry(null, moduleClasses);
    }
}
