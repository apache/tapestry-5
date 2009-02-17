// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.services.TapestryModule;
import org.slf4j.Logger;

import java.util.Formatter;
import java.util.List;

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
    private final Logger logger;

    private final SymbolProvider appProvider;

    private final String appName;

    private final String aliasMode;

    private final long startTime;

    private final RegistryBuilder builder = new RegistryBuilder();

    private long registryCreatedTime;
    private Registry registry;

    public TapestryAppInitializer(Logger logger, String appPackage, String appName, String aliasMode)
    {
        this(logger, new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage), appName,
             aliasMode);
    }

    /**
     * @param logger      logger for output confirmation
     * @param appProvider provides symbols for the application (normally, from the ServletContext init parameters)
     * @param appName     the name of the application (i.e., the name of the application servlet)
     * @param aliasMode   the mode, used by the {@link org.apache.tapestry5.services.Alias} service, normally "servlet"
     */
    public TapestryAppInitializer(Logger logger, SymbolProvider appProvider, String appName, String aliasMode)
    {
        this.logger = logger;
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

        addSyntheticSymbolSourceModule(appPackage);
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

    public void addModules(Class... moduleClasses)
    {
        builder.add(moduleClasses);
    }

    private void addSyntheticSymbolSourceModule(String appPackage)
    {
        ContributionDef appPathContribution =
                new SyntheticSymbolSourceContributionDef("AppPath",
                                                         new SingleKeySymbolProvider(
                                                                 InternalSymbols.APP_PACKAGE_PATH,
                                                                 appPackage.replace('.', '/')));

        ContributionDef symbolSourceContribution =
                new SyntheticSymbolSourceContributionDef("ServletContext",
                                                         appProvider,
                                                         "before:ApplicationDefaults");

        ContributionDef aliasModeContribution =
                new SyntheticSymbolSourceContributionDef("AliasMode",
                                                         new SingleKeySymbolProvider(InternalSymbols.ALIAS_MODE,
                                                                                     aliasMode),
                                                         "before:ServletContext");

        ContributionDef appNameContribution =
                new SyntheticSymbolSourceContributionDef("AppName",
                                                         new SingleKeySymbolProvider(InternalSymbols.APP_NAME, appName),
                                                         "before:ServletContext");

        builder.add(new SyntheticModuleDef(symbolSourceContribution, aliasModeContribution, appNameContribution,
                                           appPathContribution));
    }

    public Registry createRegistry()
    {
        registryCreatedTime = System.currentTimeMillis();

        registry = builder.build();

        return registry;
    }

    public void announceStartup()
    {
        long toFinish = System.currentTimeMillis();

        SymbolSource source = registry.getService("SymbolSource", SymbolSource.class);

        StringBuilder buffer = new StringBuilder("Startup status:\n\n");
        Formatter f = new Formatter(buffer);

        f.format("Application '%s' (Tapestry version %s).\n\n" +
                "Startup time: %,d ms to build IoC Registry, %,d ms overall.\n\n" +
                "Startup services status:\n",
                 appName,
                 source.valueForSymbol(SymbolConstants.TAPESTRY_VERSION),
                 registryCreatedTime - startTime, toFinish - startTime);

        int unrealized = 0;

        ServiceActivityScoreboard scoreboard = registry
                .getService(ServiceActivityScoreboard.class);

        List<ServiceActivity> serviceActivity = scoreboard.getServiceActivity();

        int longest = 0;

        // One pass to find the longest name, and to count the unrealized services.

        for (ServiceActivity activity : serviceActivity)
        {
            Status status = activity.getStatus();

            longest = Math.max(longest, activity.getServiceId().length());

            if (status == Status.DEFINED || status == Status.VIRTUAL) unrealized++;
        }

        String formatString = "%" + longest + "s: %s\n";

        // A second pass to output all the services

        for (ServiceActivity activity : serviceActivity)
        {
            f.format(formatString, activity.getServiceId(), activity.getStatus().name());
        }

        f.format("\n%4.2f%% unrealized services (%d/%d)\n", 100. * unrealized / serviceActivity.size(), unrealized,
                 serviceActivity.size());

        logger.info(buffer.toString());
    }
}
