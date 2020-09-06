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

package org.apache.tapestry5.http.internal;

import java.util.Formatter;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.modules.TapestryHttpModule;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ServiceActivity;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.Status;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;

/**
 * This class is used to build the {@link Registry}. The Registry contains
 * {@link org.apache.tapestry5.ioc.modules.TapestryIOCModule} and {@link TapestryHttpModule}, any
 * modules identified by {@link #addModules(Class[])} )}, plus the application module.
 *
 * The application module is optional.
 *
 * The application module is identified as <em>package</em>.services.<em>appName</em>Module, where
 * <em>package</em> and the <em>appName</em> are specified by the caller.
 */
@SuppressWarnings("rawtypes")
public class TapestryAppInitializer
{
    private final Logger logger;

    private final SymbolProvider appProvider;

    private final String appName;

    private final long startTime;

    private final RegistryBuilder builder = new RegistryBuilder();

    private long registryCreatedTime;

    private Registry registry;

    /**
     * @param logger
     *         logger for output confirmation
     * @param appPackage
     *         root package name to search for pages and components
     * @param appName
     *         the name of the application (i.e., the name of the application servlet)
     */
    public TapestryAppInitializer(Logger logger, String appPackage, String appName)
    {
        this(logger, new SingleKeySymbolProvider(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM, appPackage), appName,
                null);
    }

    /**
     * @param logger
     *         logger for output confirmation
     * @param appProvider
     *         provides symbols for the application (normally, from the ServletContext init
     *         parameters), plus (as of 5.4) the value for symbol {@link TapestryHttpSymbolConstants#CONTEXT_PATH}
     * @param appName
     *         the name of the application (i.e., the name of the application servlet)
     * @param executionModes
     *         an optional, comma-separated list of execution modes, each of which is used
     *         to find a list of additional module classes to load (key
     *         <code>tapestry.<em>name</em>-modules</code> in appProvider, i.e., the servlet
     *         context)
     */
    public TapestryAppInitializer(Logger logger, SymbolProvider appProvider, String appName, String executionModes)
    {
        this.logger = logger;
        this.appProvider = appProvider;

        String appPackage = appProvider.valueForSymbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM);

        this.appName = appName;

        startTime = System.currentTimeMillis();

        if (!Boolean.parseBoolean(appProvider.valueForSymbol(TapestryHttpInternalConstants.DISABLE_DEFAULT_MODULES_PARAM)))
        {
            IOCUtilities.addDefaultModules(builder);
        }

        // This gets added automatically.

        addModules(TapestryHttpModule.class);

        String className = appPackage + ".services." + InternalUtils.capitalize(this.appName) + "Module";

        try
        {
            // This class is possibly loaded by a parent class loader of the application class
            // loader. The context class loader should have the appropriate view to the module
            // class,
            // if any.

            Class moduleClass = Thread.currentThread().getContextClassLoader().loadClass(className);

            builder.add(moduleClass);
        } catch (ClassNotFoundException ex)
        {
            // That's OK, not all applications will have a module class, even though any
            // non-trivial application will.
            logger.warn("Application Module class {} not found", className);
        }

        // Add a synthetic module that contributes symbol sources.

        addSyntheticSymbolSourceModule(appPackage);

        for (String mode : splitAtCommas(executionModes))
        {
            String key = String.format("tapestry.%s-modules", mode);
            String moduleList = appProvider.valueForSymbol(key);

            for (String moduleClassName : splitAtCommas(moduleList))
            {
                builder.add(moduleClassName);
            }
        }
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
        ContributionDef appPathContribution = new SyntheticSymbolSourceContributionDef("AppPath",
                new SingleKeySymbolProvider(TapestryHttpInternalSymbols.APP_PACKAGE_PATH, appPackage.replace('.', '/')));

        ContributionDef symbolSourceContribution = new SyntheticSymbolSourceContributionDef("ServletContext",
                appProvider, "before:ApplicationDefaults", "after:EnvironmentVariables");

        ContributionDef appNameContribution = new SyntheticSymbolSourceContributionDef("AppName",
                new SingleKeySymbolProvider(TapestryHttpInternalSymbols.APP_NAME, appName), "before:ServletContext");

        builder.add(new SyntheticModuleDef(symbolSourceContribution, appNameContribution, appPathContribution));
    }

    public Registry createRegistry()
    {
        registryCreatedTime = System.currentTimeMillis();

        registry = builder.build();

        return registry;
    }

    /**
     * Announce application startup, by logging (at INFO level) the names of all pages,
     * components, mixins and services.
     */
    public void announceStartup()
    {
        if (!logger.isInfoEnabled()) // if info logging is off we can stop now
        {
            return;
        }
        long toFinish = System.currentTimeMillis();

        SymbolSource source = registry.getService("SymbolSource", SymbolSource.class);

        StringBuilder buffer = new StringBuilder("Startup status:\n\nServices:\n\n");
        Formatter f = new Formatter(buffer);


        int unrealized = 0;

        ServiceActivityScoreboard scoreboard = registry.getService(ServiceActivityScoreboard.class);

        List<ServiceActivity> serviceActivity = scoreboard.getServiceActivity();

        int longest = 0;

        // One pass to find the longest name, and to count the unrealized services.

        for (ServiceActivity activity : serviceActivity)
        {
            Status status = activity.getStatus();

            longest = Math.max(longest, activity.getServiceId().length());

            if (status == Status.DEFINED || status == Status.VIRTUAL)
                unrealized++;
        }

        String formatString = "%" + longest + "s: %s\n";

        // A second pass to output all the services

        for (ServiceActivity activity : serviceActivity)
        {
            f.format(formatString, activity.getServiceId(), activity.getStatus().name());
        }

        f.format("\n%4.2f%% unrealized services (%d/%d)\n", 100. * unrealized / serviceActivity.size(), unrealized,
                serviceActivity.size());


        f.format("\nApplication '%s' (version %s) startup time: %,d ms to build IoC Registry, %,d ms overall.", appName,
                source.valueForSymbol(TapestryHttpSymbolConstants.APPLICATION_VERSION),
                registryCreatedTime - startTime,
                toFinish - startTime);

        String version = source.valueForSymbol(TapestryHttpSymbolConstants.TAPESTRY_VERSION);
        boolean productionMode = Boolean.parseBoolean(source.valueForSymbol(TapestryHttpSymbolConstants.PRODUCTION_MODE));


        buffer.append("\n\n");
        buffer.append(" ______                  __             ____\n");
        buffer.append("/_  __/__ ____  ___ ___ / /_______ __  / __/\n");
        buffer.append(" / / / _ `/ _ \\/ -_|_-</ __/ __/ // / /__ \\ \n");
        buffer.append("/_/  \\_,_/ .__/\\__/___/\\__/_/  \\_, / /____/\n");
        f.format     ("        /_/                   /___/  %s%s\n\n",
                version, productionMode ? "" : " (development mode)");

        // log multi-line string with OS-specific line endings (TAP5-2294)
        logger.info(buffer.toString().replaceAll("\\n", System.getProperty("line.separator")));
    }
    
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    private static final Pattern COMMA_PATTERN = Pattern.compile("\\s*,\\s*");

    /**
     * Splits a value around commas. Whitespace around the commas is removed, as is leading and trailing whitespace.
     *
     * @since 5.1.0.0
     */
    public static String[] splitAtCommas(String value)
    {
        if (InternalUtils.isBlank(value))
            return EMPTY_STRING_ARRAY;

        return COMMA_PATTERN.split(value.trim());
    }
    
}
