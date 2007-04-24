// Copyright 2006 The Apache Software Foundation
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

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.annotations.SubModule;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.internal.DefaultModuleDefImpl;
import org.apache.tapestry.ioc.internal.IOCMessages;
import org.apache.tapestry.ioc.internal.LogSourceImpl;
import org.apache.tapestry.ioc.internal.RegistryImpl;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.TapestryIOCModule;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

/**
 * Used to construct the IoC {@link org.apache.tapestry.ioc.Registry}. This class is <em>not</em>
 * threadsafe. The Registry, once created, <em>is</em> threadsafe.
 */
public final class RegistryBuilder
{
    private final OneShotLock _lock = new OneShotLock();

    /** Module defs, keyed on module id. */
    final Map<String, ModuleDef> _modules = newMap();

    /**
     * Service implementation overrides, keyed on service id. Service implementations are most
     * useful when perfroming integration tests on services. As one service can bring in another, we
     * have to stop at a certain "bounary" services by provide stub/ mock objects as their
     * implementations.
     */
    private final Map<String, Object> _serviceOverrides = newMap();

    private final ClassLoader _classLoader;

    private final Log _log;

    private final LogSource _logSource;

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

        add(TapestryIOCModule.class);
    }

    public void add(ModuleDef moduleDef)
    {
        _lock.check();

        String id = moduleDef.getModuleId();

        if (_modules.containsKey(id))
        {
            _log.warn(IOCMessages.moduleIdConflict(id), null);
            return;
        }

        _modules.put(id, moduleDef);
    }

    public void add(Class... moduleBuilderClasses)
    {
        _lock.check();

        List<Class> queue = newList(Arrays.asList(moduleBuilderClasses));

        while (!queue.isEmpty())
        {
            Class c = queue.remove(0);

            ModuleDef def = new DefaultModuleDefImpl(c, _log);
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

        return new RegistryImpl(_modules.values(), _classLoader, _logSource, _serviceOverrides);
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

    /**
     * Adds a service override, which is used in certain testing situations to allow deeply
     * entrenched services to be selectively overriden with alternate implementations. This is not
     * intended for use with deployed applications, only in testing.
     * 
     * @param serviceId
     *            fully qualified service id
     * @param overridingImpl
     *            overriding implementation of the service (the service's normal service builder
     *            method will not be invoked)
     */
    public void addServiceOverride(String serviceId, Object overridingImpl)
    {
        _serviceOverrides.put(serviceId, overridingImpl);
    }
}
