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

package org.apache.tapestry.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.SymbolProvider;
import org.apache.tapestry.services.Alias;
import org.apache.tapestry.services.TapestryModule;
import org.apache.tapestry.test.pagelevel.PageTester;

/**
 * This class is used to build the {@link Registry}. The Registry contains
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
    private final SymbolProvider _appProvider;

    private final String _appPackage;

    private final String _appName;

    private final String _aliasMode;

    private final Registry _registry;

    private final long _startTime;

    private final long _registryCreatedTime;

    private final Map<String, Object> _serviceOverrides;

    public TapestryAppInitializer(String appPackage, String appName, String aliasMode)
    {
        this(new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage),
                appName, aliasMode);
    }

    public TapestryAppInitializer(SymbolProvider appProvider, String appName, String aliasMode,
            ModuleDef... moduleDefs)
    {
        this(appProvider, appName, aliasMode, null, moduleDefs);
    }

    /**
     * @param appProvider
     *            provides symbols for the application (normally, from the ServletContext init
     *            parameters)
     * @param appName
     *            the name of the application (i.e., the name of the application servlet)
     * @param aliasMode
     *            the mode, used by the {@link Alias} service, normally "servlet"
     * @param serviceOverrides
     *            specific service overrides (used by {@link PageTester}
     * @param moduleDefs
     *            additional module definitions to be mixed in to those automatically located
     */
    public TapestryAppInitializer(SymbolProvider appProvider, String appName, String aliasMode,
            Map<String, Object> serviceOverrides, ModuleDef... moduleDefs)
    {
        _appProvider = appProvider;

        _appPackage = _appProvider.valueForSymbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM);

        _appName = appName;
        _aliasMode = aliasMode;
        _serviceOverrides = serviceOverrides;

        _startTime = System.currentTimeMillis();

        _registry = createRegistry(moduleDefs);

        _registryCreatedTime = System.currentTimeMillis();
    }

    private Registry createRegistry(ModuleDef... moduleDefs)
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

        // Add any explicitly provided module defs

        for (ModuleDef def : moduleDefs)
            builder.add(def);

        // Add a synthetic module that contributes symbol sources.

        addSyntheticSymbolSourceModule(builder);

        overrideServices(builder);

        return builder.build();
    }

    private void addSyntheticSymbolSourceModule(RegistryBuilder builder)
    {
        ContributionDef symbolSourceContribution = new SyntheticSymbolSourceContributionDef(
                "ServletContext", _appProvider, "before:ApplicationDefaults");

        ContributionDef aliasModeContribution = new SyntheticSymbolSourceContributionDef(
                "AliasMode", new SingleKeySymbolProvider(
                        InternalConstants.TAPESTRY_ALIAS_MODE_SYMBOL, _aliasMode),
                "before:ServletContext");

        ContributionDef appNameContribution = new SyntheticSymbolSourceContributionDef("AppName",
                new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_NAME_SYMBOL, _appName),
                "before:ServletContext");

        builder.add(new SyntheticModuleDef(symbolSourceContribution, aliasModeContribution,
                appNameContribution));
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
