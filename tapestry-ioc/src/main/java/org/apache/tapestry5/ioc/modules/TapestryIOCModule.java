// Copyright 2006-2014 The Apache Software Foundation
//
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

package org.apache.tapestry5.ioc.modules;

import org.apache.tapestry5.beanmodel.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.commons.internal.BasicTypeCoercions;
import org.apache.tapestry5.commons.internal.services.*;
import org.apache.tapestry5.commons.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.TimeInterval;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.ioc.IOCSymbols;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.internal.services.AspectDecoratorImpl;
import org.apache.tapestry5.ioc.internal.services.AutobuildObjectProvider;
import org.apache.tapestry5.ioc.internal.services.ChainBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.ClassNameLocatorImpl;
import org.apache.tapestry5.ioc.internal.services.ClasspathScannerImpl;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.services.DefaultImplementationBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.ExceptionAnalyzerImpl;
import org.apache.tapestry5.ioc.internal.services.ExceptionTrackerImpl;
import org.apache.tapestry5.ioc.internal.services.LazyAdvisorImpl;
import org.apache.tapestry5.ioc.internal.services.LoggingAdvisorImpl;
import org.apache.tapestry5.ioc.internal.services.LoggingDecoratorImpl;
import org.apache.tapestry5.ioc.internal.services.MapSymbolProvider;
import org.apache.tapestry5.ioc.internal.services.MasterObjectProviderImpl;
import org.apache.tapestry5.ioc.internal.services.NonParallelExecutor;
import org.apache.tapestry5.ioc.internal.services.OperationAdvisorImpl;
import org.apache.tapestry5.ioc.internal.services.ParallelExecutorImpl;
import org.apache.tapestry5.ioc.internal.services.PerThreadServiceLifecycle;
import org.apache.tapestry5.ioc.internal.services.PipelineBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.PropertyShadowBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.RegistryStartup;
import org.apache.tapestry5.ioc.internal.services.ServiceOverrideImpl;
import org.apache.tapestry5.ioc.internal.services.StrategyBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.SymbolObjectProvider;
import org.apache.tapestry5.ioc.internal.services.SymbolSourceImpl;
import org.apache.tapestry5.ioc.internal.services.SystemEnvSymbolProvider;
import org.apache.tapestry5.ioc.internal.services.SystemPropertiesSymbolProvider;
import org.apache.tapestry5.ioc.internal.services.ThreadLocaleImpl;
import org.apache.tapestry5.ioc.internal.services.ThunkCreatorImpl;
import org.apache.tapestry5.ioc.internal.services.UpdateListenerHubImpl;
import org.apache.tapestry5.ioc.internal.services.ValueObjectProvider;
import org.apache.tapestry5.ioc.internal.services.cron.PeriodicExecutorImpl;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.services.ClasspathScanner;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.DefaultImplementationBuilder;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionTracker;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.LazyAdvisor;
import org.apache.tapestry5.ioc.services.LoggingAdvisor;
import org.apache.tapestry5.ioc.services.LoggingDecorator;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.OperationAdvisor;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PipelineBuilder;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.ServiceConfigurationListenerHub;
import org.apache.tapestry5.ioc.services.ServiceLifecycleSource;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.ioc.services.StrategyBuilder;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.services.ThunkCreator;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.tapestry5.ioc.OrderConstraintBuilder.after;
import static org.apache.tapestry5.ioc.OrderConstraintBuilder.before;

/**
 * Defines the base set of services for the Tapestry IOC container.
 */
@SuppressWarnings("all")
@Marker(Builtin.class)
public final class TapestryIOCModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(LoggingDecorator.class, LoggingDecoratorImpl.class);
        binder.bind(ChainBuilder.class, ChainBuilderImpl.class);
        binder.bind(PropertyAccess.class, PropertyAccessImpl.class);
        binder.bind(StrategyBuilder.class, StrategyBuilderImpl.class);
        binder.bind(PropertyShadowBuilder.class, PropertyShadowBuilderImpl.class);
        binder.bind(PipelineBuilder.class, PipelineBuilderImpl.class).preventReloading();
        binder.bind(DefaultImplementationBuilder.class, DefaultImplementationBuilderImpl.class);
        binder.bind(ExceptionTracker.class, ExceptionTrackerImpl.class);
        binder.bind(ExceptionAnalyzer.class, ExceptionAnalyzerImpl.class);
        binder.bind(TypeCoercer.class, TypeCoercerImpl.class).preventReloading();
        binder.bind(ThreadLocale.class, ThreadLocaleImpl.class);
        binder.bind(SymbolSource.class, SymbolSourceImpl.class);
        binder.bind(SymbolProvider.class, MapSymbolProvider.class).withId("ApplicationDefaults")
                .withMarker(ApplicationDefaults.class);
        binder.bind(SymbolProvider.class, MapSymbolProvider.class).withId("FactoryDefaults")
                .withMarker(FactoryDefaults.class);
        binder.bind(Runnable.class, RegistryStartup.class).withSimpleId();
        binder.bind(MasterObjectProvider.class, MasterObjectProviderImpl.class).preventReloading();
        binder.bind(ClassNameLocator.class, ClassNameLocatorImpl.class);
        binder.bind(ClasspathScanner.class, ClasspathScannerImpl.class);
        binder.bind(AspectDecorator.class, AspectDecoratorImpl.class);
        binder.bind(ClasspathURLConverter.class, ClasspathURLConverterImpl.class);
        binder.bind(ServiceOverride.class, ServiceOverrideImpl.class);
        binder.bind(LoggingAdvisor.class, LoggingAdvisorImpl.class);
        binder.bind(LazyAdvisor.class, LazyAdvisorImpl.class);
        binder.bind(ThunkCreator.class, ThunkCreatorImpl.class);
        binder.bind(UpdateListenerHub.class, UpdateListenerHubImpl.class).preventReloading();
        binder.bind(PeriodicExecutor.class, PeriodicExecutorImpl.class);
        binder.bind(OperationAdvisor.class, OperationAdvisorImpl.class);
        binder.bind(ServiceConfigurationListenerHub.class);
    }

    /**
     * Provides access to additional service lifecycles. One lifecycle is built in ("singleton") but additional ones are
     * accessed via this service (and its mapped configuration). Only proxiable services (those with explicit service
     * interfaces) can be managed in terms of a lifecycle.
     */
    @PreventServiceDecoration
    public static ServiceLifecycleSource build(Map<String, ServiceLifecycle> configuration)
    {
        final Map<String, ServiceLifecycle2> lifecycles = CollectionFactory.newCaseInsensitiveMap();

        for (Entry<String, ServiceLifecycle> entry : configuration.entrySet())
        {
            lifecycles.put(entry.getKey(), InternalUtils.toServiceLifecycle2(entry.getValue()));
        }

        return new ServiceLifecycleSource()
        {
            @Override
            public ServiceLifecycle get(String scope)
            {
                return lifecycles.get(scope);
            }
        };
    }

    /**
     * Contributes the "perthread" scope.
     */
    @Contribute(ServiceLifecycleSource.class)
    public static void providePerthreadScope(MappedConfiguration<String, ServiceLifecycle> configuration)
    {
        configuration.addInstance(ScopeConstants.PERTHREAD, PerThreadServiceLifecycle.class);
    }

    /**
     * <dl>
     * <dt>AnnotationBasedContributions</dt>
     * <dd>Empty placeholder used to separate annotation-based ObjectProvider contributions (which come before) from
     * non-annotation based (such as ServiceOverride) which come after.</dd>
     * <dt>Value</dt>
     * <dd>Supports the {@link org.apache.tapestry5.ioc.annotations.Value} annotation</dd>
     * <dt>Symbol</dt>
     * <dd>Supports the {@link org.apache.tapestry5.ioc.annotations.Symbol} annotations</dd>
     * <dt>Autobuild</dt>
     * <dd>Supports the {@link org.apache.tapestry5.ioc.annotations.Autobuild} annotation</dd>
     * <dt>ServiceOverride</dt>
     * <dd>Allows simple service overrides via the {@link org.apache.tapestry5.ioc.services.ServiceOverride} service
     * (and its configuration)
     * </dl>
     */
    @Contribute(MasterObjectProvider.class)
    public static void setupObjectProviders(OrderedConfiguration<ObjectProvider> configuration, @Local
    final ServiceOverride serviceOverride)
    {
        configuration.add("AnnotationBasedContributions", null);

        configuration.addInstance("Value", ValueObjectProvider.class, before("AnnotationBasedContributions").build());
        configuration.addInstance("Symbol", SymbolObjectProvider.class, before("AnnotationBasedContributions").build());
        configuration.add("Autobuild", new AutobuildObjectProvider(), before("AnnotationBasedContributions").build());

        ObjectProvider wrapper = new ObjectProvider()
        {
            @Override
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                return serviceOverride.getServiceOverrideProvider().provide(objectType, annotationProvider, locator);
            }
        };

        configuration.add("ServiceOverride", wrapper, after("AnnotationBasedContributions").build());
    }

    /**
     * Contributes a set of standard type coercions to the {@link TypeCoercer} service:
     * <ul>
     * <li>Object to String</li>
     * <li>Object to Boolean</li>
     * <li>String to Double</li>
     * <li>String to BigDecimal</li>
     * <li>BigDecimal to Double</li>
     * <li>Double to BigDecimal</li>
     * <li>String to BigInteger</li>
     * <li>BigInteger to Long</li>
     * <li>String to Long</li>
     * <li>Long to Byte</li>
     * <li>Long to Short</li>
     * <li>Long to Integer</li>
     * <li>Double to Long</li>
     * <li>Double to Float</li>
     * <li>Float to Double</li>
     * <li>Long to Double</li>
     * <li>String to Boolean ("false" is always false, other non-blank strings are true)</li>
     * <li>Number to Boolean (true if number value is non zero)</li>
     * <li>Null to Boolean (always false)</li>
     * <li>Collection to Boolean (false if empty)</li>
     * <li>Object[] to List</li>
     * <li>primitive[] to List</li>
     * <li>Object to List (by wrapping as a singleton list)</li>
     * <li>String to File</li>
     * <li>String to {@link org.apache.tapestry5.commons.util.TimeInterval}</li>
     * <li>{@link org.apache.tapestry5.commons.util.TimeInterval} to Long</li>
     * <li>Object to Object[] (wrapping the object as an array)</li>
     * <li>Collection to Object[] (via the toArray() method)
     * <li>{@link Flow} to List</li>
     * <li>{@link Flow} to Boolean (false if empty)</li>
     * </ul>
     */
    @Contribute(TypeCoercer.class)
    public static void provideBasicTypeCoercions(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration)
    {
        BasicTypeCoercions.provideBasicTypeCoercions(configuration);
    }
    
    /**
     * Contributes coercions to and from Java Time API (JSR 310) classes.
     * <ul>
     * <li>java.time.Year to Integer</li>
     * <li>Integer to java.time.Year</li>
     * <li>java.time.Month to Integer</li>
     * <li>Integer to Java.time.Month</li>
     * <li>java.time.Month to String</li>
     * <li>String to java.time.Month</li>
     * <li>String to java.time.YearMonth</li>
     * <li>java.time.YearMonth to java.time.Year</li>
     * <li>java.time.YearMonth to java.time.Month</li>
     * <li>String to java.time.MonthDay</li>
     * <li>java.time.MonthDay to java.time.Month</li>
     * <li>java.time.DayOfWeek to Integer</li>
     * <li>Integer to java.time.DayOfWeek</li>
     * <li>java.time.DayOfWeek to String</li>
     * <li>String to java.time.DayOfWeek</li>
     * <li>java.time.LocalDate to java.time.Instant</li>
     * <li>java.time.Instant to java.time.LocalDate</li>
     * <li>String to java.time.LocalDate</li>
     * <li>java.time.LocalDate to java.time.YearMonth</li>
     * <li>java.time.LocalDate to java.time.MonthDay</li>
     * <li>java.time.LocalTime to Long</li>
     * <li>Long to java.time.LocalTime</li>
     * <li>String to java.time.LocalDateTime</li>
     * <li>java.time.LocalDateTime to java.time.Instant</li>
     * <li>java.time.Instant to LocalDateTime</li>
     * <li>java.time.LocalDateTime to java.time.LocalDate</li>
     * <li>String to java.time.OffsetDateTime</li>
     * <li>java.time.OffsetDateTime to java.time.Instant</li>
     * <li>java.time.Instant to java.time.OffsetDateTime</li>
     * <li>String to java.time.ZoneId</li>
     * <li>String to java.time.ZoneOffset</li>
     * <li>String to java.time.ZonedDateTime</li>
     * <li>java.time.ZonedDateTime to java.time.Instant</li>
     * <li>java.time.ZonedDateTime to java.time.ZoneId</li>
     * <li>java.time.Instant to Long</li>
     * <li>Long to java.time.Instant</li>
     * <li>java.time.Instant to java.util.Date</li>
     * <li>java.util.Date to java.time.Instant</li>
     * <li>java.time.Duration to Long</li>
     * <li>Long to java.time.Duration</li>
     * <li>String to java.time.Period</li>
     * </ul>
     */
    @Contribute(TypeCoercer.class)
    public static void provideJSR10TypeCoercions(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration)
    {
        BasicTypeCoercions.provideJSR310TypeCoercions(configuration);
    }
    
    /**
     * <dl>
     * <dt>SystemProperties</dt>
     * <dd>Exposes JVM System properties as symbols (currently case-sensitive)</dd>
     * <dt>EnvironmentVariables</dt>
     * <dd>Exposes environment variables as symbols (adding a "env." prefix)</dd>
     * <dt>ApplicationDefaults</dt>
     * <dd>Values contributed to @{@link SymbolProvider} @{@link ApplicationDefaults}</dd>
     * <dt>FactoryDefaults</dt>
     * <dd>Values contributed to @{@link SymbolProvider} @{@link FactoryDefaults}</dd>
     * </dl>
     */
    @Contribute(SymbolSource.class)
    public static void setupStandardSymbolProviders(OrderedConfiguration<SymbolProvider> configuration,
                                                    @ApplicationDefaults
                                                    SymbolProvider applicationDefaults,

                                                    @FactoryDefaults
                                                    SymbolProvider factoryDefaults)
    {
        configuration.add("SystemProperties", new SystemPropertiesSymbolProvider(), "before:*");
        configuration.add("EnvironmentVariables", new SystemEnvSymbolProvider());
        configuration.add("ApplicationDefaults", applicationDefaults);
        configuration.add("FactoryDefaults", factoryDefaults);
    }

    public static ParallelExecutor buildDeferredExecution(@Symbol(IOCSymbols.THREAD_POOL_CORE_SIZE)
                                                          int coreSize,

                                                          @Symbol(IOCSymbols.THREAD_POOL_MAX_SIZE)
                                                          int maxSize,

                                                          @Symbol(IOCSymbols.THREAD_POOL_KEEP_ALIVE)
                                                          @IntermediateType(TimeInterval.class)
                                                          int keepAliveMillis,

                                                          @Symbol(IOCSymbols.THREAD_POOL_ENABLED)
                                                          boolean threadPoolEnabled,

                                                          @Symbol(IOCSymbols.THREAD_POOL_QUEUE_SIZE)
                                                          int queueSize,

                                                          PerthreadManager perthreadManager,

                                                          RegistryShutdownHub shutdownHub,

                                                          ThunkCreator thunkCreator)
    {

        if (!threadPoolEnabled)
            return new NonParallelExecutor();

        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(queueSize);

        final ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSize, maxSize, keepAliveMillis,
                TimeUnit.MILLISECONDS, workQueue);

        shutdownHub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                executorService.shutdown();
            }
        });

        return new ParallelExecutorImpl(executorService, thunkCreator, perthreadManager);
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void setupDefaultSymbols(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(IOCSymbols.THREAD_POOL_CORE_SIZE, 3);
        configuration.add(IOCSymbols.THREAD_POOL_MAX_SIZE, 20);
        configuration.add(IOCSymbols.THREAD_POOL_KEEP_ALIVE, "1 m");
        configuration.add(IOCSymbols.THREAD_POOL_ENABLED, true);
        configuration.add(IOCSymbols.THREAD_POOL_QUEUE_SIZE, 100);
    }
    
    public static void contributeRegistryStartup(OrderedConfiguration<Runnable> configuration, 
            PeriodicExecutor periodicExecutor)
    {
        configuration.add(PeriodicExecutor.class.getSimpleName(), 
                () ->  periodicExecutor.init());
    }
    
}
