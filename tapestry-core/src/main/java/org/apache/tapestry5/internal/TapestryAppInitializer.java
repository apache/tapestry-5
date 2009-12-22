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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.Alias;
import org.apache.tapestry5.services.TapestryModule;

/**
 * This class is used to build the {@link Registry}. The Registry contains {@link org.apache.tapestry5.ioc.services.TapestryIOCModule}
 * and {@link TapestryModule}, any modules identified by {@link #addModules(Class[])} )}, plus the application module.
 * <p/>
 * The application module is optional.
 * <p/>
 * The application module is identified as <em>package</em>.services.<em>appName</em>Module, where <em>package</em> and
 * the <em>appName</em> are specified by the caller.
 */
public class TapestryAppInitializer
{
    private final SymbolProvider appProvider;

    private final String appName;

    private final String aliasMode;

    private final long startTime;

    private final RegistryBuilder builder = new RegistryBuilder();

    private long registryCreatedTime;

    public TapestryAppInitializer(String appPackage, String appName, String aliasMode)
    {
        this(new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage), appName, aliasMode);
    }

    /**
     * @param appProvider provides symbols for the application (normally, from the ServletContext init parameters)
     * @param appName     the name of the application (i.e., the name of the application servlet)
     * @param aliasMode   the mode, used by the {@link Alias} service, normally "servlet"
     */
    public TapestryAppInitializer(SymbolProvider appProvider, String appName, String aliasMode)
    {
        this.appProvider = appProvider;

        String appPackage = appProvider.valueForSymbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM);


        this.appName = appName;
        this.aliasMode = aliasMode;

        startTime = System.currentTimeMillis();


        if (!Boolean.parseBoolean(appProvider.valueForSymbol(InternalConstants.DISABLE_DEFAULT_MODULES_PARAM)))
        {
            IOCUtilities.addDefaultModules(builder);
        }

        // This gets added automatically.

        addModules(TapestryModule.class);

        String className = appPackage + ".services." + InternalUtils.capitalize(this.appName) + "Module";

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

        // Add a synthetic module that contributes symbol sources.

        addSyntheticSymbolSourceModule();
    }

    /**
     * Adds additional modules.
     *
     * @param moduleDefs
     */
    public void addModules(ModuleDef... moduleDefs)
    {
        for (ModuleDef def : moduleDefs)
            builder.add(def);
    }

    public void addModules(Class... moduleBuilderClasses)
    {
        builder.add(moduleBuilderClasses);
    }

    private void addSyntheticSymbolSourceModule()
    {
        ContributionDef symbolSourceContribution = new SyntheticSymbolSourceContributionDef("ServletContext",
                                                                                            appProvider,
                                                                                            "before:ApplicationDefaults");

        ContributionDef aliasModeContribution = new SyntheticSymbolSourceContributionDef("AliasMode",
                                                                                         new SingleKeySymbolProvider(
                                                                                                 InternalConstants.TAPESTRY_ALIAS_MODE_SYMBOL,
                                                                                                 aliasMode),
                                                                                         "before:ServletContext");

        ContributionDef appNameContribution = new SyntheticSymbolSourceContributionDef("AppName",
                                                                                       new SingleKeySymbolProvider(
                                                                                               InternalConstants.TAPESTRY_APP_NAME_SYMBOL,
                                                                                               appName),
                                                                                       "before:ServletContext");

        builder.add(new SyntheticModuleDef(symbolSourceContribution, aliasModeContribution, appNameContribution));
    }

    public Registry getRegistry()
    {
        registryCreatedTime = System.currentTimeMillis();

        return builder.build();
    }

    /**
     * @return the system time (in ms) when the registry has been created successfully.
     */
    public long getRegistryCreatedTime()
    {
        return registryCreatedTime;
    }

    /**
     * @return the time when the initialization was started.
     */
    public long getStartTime()
    {
        return startTime;
    }
}
