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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.*;
import static org.apache.tapestry5.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Value;
import org.apache.tapestry5.ioc.internal.services.*;
import org.apache.tapestry5.ioc.util.TimeInterval;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Defines the base set of services for the Tapestry IOC container.
 */
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
        binder.bind(PipelineBuilder.class, PipelineBuilderImpl.class);
        binder.bind(DefaultImplementationBuilder.class, DefaultImplementationBuilderImpl.class);
        binder.bind(ExceptionTracker.class, ExceptionTrackerImpl.class);
        binder.bind(ExceptionAnalyzer.class, ExceptionAnalyzerImpl.class);
        binder.bind(TypeCoercer.class, TypeCoercerImpl.class);
        binder.bind(ThreadLocale.class, ThreadLocaleImpl.class);
        binder.bind(SymbolSource.class, SymbolSourceImpl.class);
        binder.bind(SymbolProvider.class, MapSymbolProvider.class).withId("ApplicationDefaults")
                .withMarker(ApplicationDefaults.class);
        binder.bind(SymbolProvider.class, MapSymbolProvider.class).withId("FactoryDefaults")
                .withMarker(FactoryDefaults.class);
        binder.bind(Runnable.class, RegistryStartup.class).withId("RegistryStartup");
        binder.bind(MasterObjectProvider.class, MasterObjectProviderImpl.class);
        binder.bind(ClassNameLocator.class, ClassNameLocatorImpl.class);
        binder.bind(AspectDecorator.class, AspectDecoratorImpl.class);
    }

    /**
     * Provides access to additional service lifecycles. One lifecycles is built in ("singleton") but additional ones
     * are accessed via this service (and its mapped configuration). Only proxiable services (those with explicit
     * service interfaces) can be managed in terms of a lifecycle.
     */
    public static ServiceLifecycleSource build(final Map<String, ServiceLifecycle> configuration)
    {
        return new ServiceLifecycleSource()
        {
            public ServiceLifecycle get(String lifecycleName)
            {
                return configuration.get(lifecycleName);
            }
        };
    }

    /**
     * Contributes the "perthread" scope.
     */
    public void contributeServiceLifecycleSource(MappedConfiguration<String, ServiceLifecycle> configuration,
                                                 ObjectLocator locator)
    {
        configuration.add(PERTHREAD_SCOPE, locator.autobuild(PerThreadServiceLifecycle.class));
    }

    /**
     * Contributes "DefaultProvider", ordered last, that delegates to {@link ObjectLocator#getService(Class)}.
     * <p/>
     * Contributes "Value", which injects values (not services) triggered by the {@link Value} annotation.
     */
    public static void contributeMasterObjectProvider(OrderedConfiguration<ObjectProvider> configuration,

                                                      ObjectLocator locator)
    {
        configuration.add("Value", locator.autobuild(ValueObjectProvider.class));
        configuration.add("Symbol", locator.autobuild(SymbolObjectProvider.class));
    }

    /**
     * Contributes a set of standard type coercions to the {@link TypeCoercer} service: <ul> <li>Object to String</li>
     * <li>String to Double</li> <li>String to BigDecimal</li> <li>BigDecimal to Double</li> <li>Double to
     * BigDecimal</li> <li>String to BigInteger</li> <li>BigInteger to Long</li> <li>String to Long</li> <li>Long to
     * Byte</li> <li>Long to Short</li> <li>Long to Integer</li> <li>Double to Long</li> <li>Double to Float</li>
     * <li>Float to Double</li> <li>Long to Double</li> <li>String to Boolean ("false" is always false, other non-blank
     * strings are true)</li> <li>Long to Boolean (true if long value is non zero)</li> <li>Null to Boolean (always
     * false)</li> <li>Collection to Boolean (false if empty)</li> <li>Object[] to List</li> <li>primitive[] to
     * List</li> <li>Object to List (by wrapping as a singleton list)</li>  <li>String to File</li> <li>String to {@link
     * org.apache.tapestry5.ioc.util.TimeInterval}</li> <li>{@link org.apache.tapestry5.ioc.util.TimeInterval} to
     * Long</li> </ul>
     */
    @SuppressWarnings("unchecked")
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
        add(configuration, Object.class, String.class, new Coercion<Object, String>()
        {
            public String coerce(Object input)
            {
                return input.toString();
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
                String trimmed = input.trim();

                if (trimmed.equalsIgnoreCase("false") || trimmed.length() == 0) return false;

                // Any non-blank string but "false"

                return true;
            }
        });

        add(configuration, Long.class, Boolean.class, new Coercion<Long, Boolean>()
        {
            public Boolean coerce(Long input)
            {
                return input.longValue() != 0;
            }
        });

        add(configuration, void.class, Boolean.class, new Coercion<Void, Boolean>()
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
    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType, Class<T> targetType,
                                   Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = new CoercionTuple<S, T>(sourceType, targetType, coercion);

        configuration.add(tuple);
    }

    public static void contributeSymbolSource(OrderedConfiguration<SymbolProvider> configuration,
                                              @ApplicationDefaults SymbolProvider applicationDefaults,

                                              @FactoryDefaults SymbolProvider factoryDefaults)
    {
        configuration.add("SystemProperties", new SystemPropertiesSymbolProvider());
        configuration.add("ApplicationDefaults", applicationDefaults, "after:SystemProperties");
        configuration.add("FactoryDefaults", factoryDefaults, "after:ApplicationDefaults");
    }
}
