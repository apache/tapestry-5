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

package org.apache.tapestry.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.TapestryModule;

/**
 * This class is used to build the {@link Registry}. The registry contains
 * {@link org.apache.tapestry.ioc.services.TapestryIOCModule} and {@link TapestryModule}, any
 * modules identified by {@link #addModules(RegistryBuilder)}, plus the application module.
 * <p>
 * The application module is optional.
 * <p>
 * The application module is identified as <em>package</em>.services.<em>appName</em>Module,
 * where <em>package</em> and the <em>appName</em> are specified by the caller.
 */
public class TapestryAppInitializer
{
    private String _appPackage;

    private String _appName;

    private String _infrastructureMode;

    private Registry _registry;

    private long _startTime;

    private long _registryCreatedTime;

    private final Map<String, Object> _serviceOverrides;

    public TapestryAppInitializer(String appPackage, String appName, String infrastructureMode)
    {
        this(appPackage, appName, infrastructureMode, null);
    }

    public TapestryAppInitializer(String appPackage, String appName, String infrastructureMode,
            Map<String, Object> serviceOverrides)
    {
        _appPackage = appPackage;
        _appName = appName;
        _infrastructureMode = infrastructureMode;
        _serviceOverrides = serviceOverrides;
        _startTime = System.currentTimeMillis();
        createRegistry();
        _registryCreatedTime = System.currentTimeMillis();
        setupServices();
    }

    private void createRegistry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(TapestryModule.class);

        String className = _appPackage + ".services." + InternalUtils.capitalize(_appName)
                + "Module";

        try
        {
            // This class is possibly loaded by a parent class loader of the application class
            // loader. The context class loader should have the approprite view to the module class,
            // if any.

            Class moduleClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            builder.add(moduleClass);
        }
        catch (ClassNotFoundException ex)
        {
            // That's OK, not all applications will have a module class, even though any
            // non-trivial application will.
        }

        addModules(builder);

        overrideServices(builder);

        _registry = builder.build();
    }

    private void overrideServices(RegistryBuilder builder)
    {
        if (_serviceOverrides != null)
        {
            for (Entry<String, Object> e : _serviceOverrides.entrySet())
            {
                builder.addServiceOverride(e.getKey(), e.getValue());
            }
        }
    }

    private void setupServices()
    {
        Infrastructure infra = _registry
                .getService("tapestry.Infrastructure", Infrastructure.class);
        infra.setMode(_infrastructureMode);

        ComponentClassResolver resolver = _registry.getService(
                "tapestry.ComponentClassResolver",
                ComponentClassResolver.class);

        resolver.setApplicationPackage(_appPackage);
    }

    /**
     * Adds additional modules to the builder. This implementation adds any modules identified by
     * {@link IOCUtilities#addDefaultModules(RegistryBuilder)}. Most subclasses will invoke this
     * implementation, and add additional modules to the RegistryBuilder besides.
     * {@link org.apache.tapestry.ioc.services.TapestryIOCModule} and {@link TapestryModule} will
     * already have been added, as will an application module if present.
     * 
     * @param builder
     */
    protected void addModules(RegistryBuilder builder)
    {
        IOCUtilities.addDefaultModules(builder);
    }

    public Registry getRegistry()
    {
        return _registry;
    }

    /**
     * @return the system time (in ms) when the registry has been created successfully.
     */
    public long getRegistryCreatedTime()
    {
        return _registryCreatedTime;
    }

    /**
     * @return the time when the initialization was started.
     */
    public long getStartTime()
    {
        return _startTime;
    }
}
