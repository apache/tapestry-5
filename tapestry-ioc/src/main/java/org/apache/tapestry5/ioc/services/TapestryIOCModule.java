// Copyright 2006-2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.internal.services.*;
import org.apache.tapestry5.ioc.internal.services.cron.PeriodicExecutorImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.UpdateListenerHub;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
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
        binder.bind(AspectDecorator.class, AspectDecoratorImpl.class);
        binder.bind(ClasspathURLConverter.class, ClasspathURLConverterImpl.class);
        binder.bind(ServiceOverride.class, ServiceOverrideImpl.class);
        binder.bind(LoggingAdvisor.class, LoggingAdvisorImpl.class);
        binder.bind(LazyAdvisor.class, LazyAdvisorImpl.class);
        binder.bind(ThunkCreator.class, ThunkCreatorImpl.class);
        binder.bind(UpdateListenerHub.class, UpdateListenerHubImpl.class).preventReloading();
        binder.bind(PeriodicExecutor.class, PeriodicExecutorImpl.class);
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

        for (String name : configuration.keySet())
        {
            lifecycles.put(name, InternalUtils.toServiceLifecycle2(configuration.get(name)));
        }

        return new ServiceLifecycleSource()
        {
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
     * <li>String to {@link org.apache.tapestry5.ioc.util.TimeInterval}</li>
     * <li>{@link org.apache.tapestry5.ioc.util.TimeInterval} to Long</li>
     * <li>Object to Object[] (wrapping the object as an array)</li>
     * <li>Collection to Object[] (via the toArray() method)
     * <li>{@link Flow} to List</li>
     * <li>{@link Flow} to Boolean (false if empty)</li>
     * </ul>
     */
    @Contribute(TypeCoercer.class)
    public static void provideBasicTypeCoercions(Configuration<CoercionTuple> configuration)
    {
        add(configuration, Object.class, String.class, new Coercion<Object, String>()
        {
            public String coerce(Object input)
            {
                return input.toString();
            }
        });

        add(configuration, Object.class, Boolean.class, new Coercion<Object, Boolean>()
        {
            public Boolean coerce(Object input)
            {
                return input != null;
            }
        });

        add(configuration, String.class, Double.class, new Coercion<String, Double>()
        {
            public Double coerce(String input)
            {
                return new Double(input);
            }
        });

        // String to BigDecimal is important, as String->Double->BigDecimal would lose
        // precision.

        add(configuration, String.class, BigDecimal.class, new Coercion<String, BigDecimal>()
        {
            public BigDecimal coerce(String input)
            {
                return new BigDecimal(input);
            }
        });

        add(configuration, BigDecimal.class, Double.class, new Coercion<BigDecimal, Double>()
        {
            public Double coerce(BigDecimal input)
            {
                return input.doubleValue();
            }
        });

        add(configuration, String.class, BigInteger.class, new Coercion<String, BigInteger>()
        {
            public BigInteger coerce(String input)
            {
                return new BigInteger(input);
            }
        });

        add(configuration, String.class, Long.class, new Coercion<String, Long>()
        {
            public Long coerce(String input)
            {
                return new Long(input);
            }
        });

        add(configuration, Long.class, Byte.class, new Coercion<Long, Byte>()
        {
            public Byte coerce(Long input)
            {
                return input.byteValue();
            }
        });

        add(configuration, Long.class, Short.class, new Coercion<Long, Short>()
        {
            public Short coerce(Long input)
            {
                return input.shortValue();
            }
        });

        add(configuration, Long.class, Integer.class, new Coercion<Long, Integer>()
        {
            public Integer coerce(Long input)
            {
                return input.intValue();
            }
        });

        add(configuration, Number.class, Long.class, new Coercion<Number, Long>()
        {
            public Long coerce(Number input)
            {
                return input.longValue();
            }
        });

        add(configuration, Double.class, Float.class, new Coercion<Double, Float>()
        {
            public Float coerce(Double input)
            {
                return input.floatValue();
            }
        });

        add(configuration, Long.class, Double.class, new Coercion<Long, Double>()
        {
            public Double coerce(Long input)
            {
                return input.doubleValue();
            }
        });

        add(configuration, String.class, Boolean.class, new Coercion<String, Boolean>()
        {
            public Boolean coerce(String input)
            {
                String trimmed = input == null ? "" : input.trim();

                if (trimmed.equalsIgnoreCase("false") || trimmed.length() == 0)
                    return false;

                // Any non-blank string but "false"

                return true;
            }
        });

        add(configuration, Number.class, Boolean.class, new Coercion<Number, Boolean>()
        {
            public Boolean coerce(Number input)
            {
                return input.longValue() != 0;
            }
        });

        add(configuration, Void.class, Boolean.class, new Coercion<Void, Boolean>()
        {
            public Boolean coerce(Void input)
            {
                return false;
            }
        });

        add(configuration, Collection.class, Boolean.class, new Coercion<Collection, Boolean>()
        {
            public Boolean coerce(Collection input)
            {
                return !input.isEmpty();
            }
        });

        add(configuration, Object.class, List.class, new Coercion<Object, List>()
        {
            public List coerce(Object input)
            {
                return Collections.singletonList(input);
            }
        });

        add(configuration, Object[].class, List.class, new Coercion<Object[], List>()
        {
            public List coerce(Object[] input)
            {
                return Arrays.asList(input);
            }
        });

        add(configuration, Object[].class, Boolean.class, new Coercion<Object[], Boolean>()
        {
            public Boolean coerce(Object[] input)
            {
                return input != null && input.length > 0;
            }
        });

        add(configuration, Float.class, Double.class, new Coercion<Float, Double>()
        {
            public Double coerce(Float input)
            {
                return input.doubleValue();
            }
        });

        Coercion primitiveArrayCoercion = new Coercion<Object, List>()
        {
            public List<Object> coerce(Object input)
            {
                int length = Array.getLength(input);
                Object[] array = new Object[length];
                for (int i = 0; i < length; i++)
                {
                    array[i] = Array.get(input, i);
                }
                return Arrays.asList(array);
            }
        };

        add(configuration, byte[].class, List.class, primitiveArrayCoercion);
        add(configuration, short[].class, List.class, primitiveArrayCoercion);
        add(configuration, int[].class, List.class, primitiveArrayCoercion);
        add(configuration, long[].class, List.class, primitiveArrayCoercion);
        add(configuration, float[].class, List.class, primitiveArrayCoercion);
        add(configuration, double[].class, List.class, primitiveArrayCoercion);
        add(configuration, char[].class, List.class, primitiveArrayCoercion);
        add(configuration, boolean[].class, List.class, primitiveArrayCoercion);

        add(configuration, String.class, File.class, new Coercion<String, File>()
        {
            public File coerce(String input)
            {
                return new File(input);
            }
        });

        add(configuration, String.class, TimeInterval.class, new Coercion<String, TimeInterval>()
        {
            public TimeInterval coerce(String input)
            {
                return new TimeInterval(input);
            }
        });

        add(configuration, TimeInterval.class, Long.class, new Coercion<TimeInterval, Long>()
        {
            public Long coerce(TimeInterval input)
            {
                return input.milliseconds();
            }
        });

        add(configuration, Object.class, Object[].class, new Coercion<Object, Object[]>()
        {
            public Object[] coerce(Object input)
            {
                return new Object[]
                        {input};
            }
        });

        add(configuration, Collection.class, Object[].class, new Coercion<Collection, Object[]>()
        {
            public Object[] coerce(Collection input)
            {
                return input.toArray();
            }
        });

        add(configuration, Flow.class, List.class, new Coercion<Flow, List>()
        {
            public List coerce(Flow input)
            {
                return input.toList();
            }
        });

        add(configuration, Flow.class, Boolean.class, new Coercion<Flow, Boolean>()
        {
            public Boolean coerce(Flow input)
            {
                return !input.isEmpty();
            }
        });

    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType,
                                   Class<T> targetType, Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = new CoercionTuple<S, T>(sourceType, targetType, coercion);

        configuration.add(tuple);
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
}
