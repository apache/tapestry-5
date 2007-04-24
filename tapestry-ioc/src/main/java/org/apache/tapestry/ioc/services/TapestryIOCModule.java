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

package org.apache.tapestry.ioc.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.ServiceLifecycle;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Lifecycle;
import org.apache.tapestry.ioc.internal.services.ChainBuilderImpl;
import org.apache.tapestry.ioc.internal.services.DefaultImplementationBuilderImpl;
import org.apache.tapestry.ioc.internal.services.ExceptionAnalyzerImpl;
import org.apache.tapestry.ioc.internal.services.ExceptionTrackerImpl;
import org.apache.tapestry.ioc.internal.services.LoggingDecoratorImpl;
import org.apache.tapestry.ioc.internal.services.MapSymbolProvider;
import org.apache.tapestry.ioc.internal.services.MasterObjectProvider;
import org.apache.tapestry.ioc.internal.services.PerThreadServiceLifecycle;
import org.apache.tapestry.ioc.internal.services.PipelineBuilderImpl;
import org.apache.tapestry.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry.ioc.internal.services.PropertyShadowBuilderImpl;
import org.apache.tapestry.ioc.internal.services.ServiceObjectProvider;
import org.apache.tapestry.ioc.internal.services.StrategyBuilderImpl;
import org.apache.tapestry.ioc.internal.services.SymbolSourceImpl;
import org.apache.tapestry.ioc.internal.services.SystemPropertiesSymbolProvider;
import org.apache.tapestry.ioc.internal.services.ThreadLocaleImpl;
import org.apache.tapestry.ioc.internal.services.TypeCoercerImpl;
import org.apache.tapestry.ioc.util.StrategyRegistry;

/**
 * Defines the base set of services for the Tapestry IOC container.
 */
public final class TapestryIOCModule
{
    private final ClassFactory _classFactory;

    private final PropertyAccess _propertyAccess;

    public TapestryIOCModule(@InjectService("ClassFactory")
    ClassFactory classFactory,

    @InjectService("PropertyAccess")
    PropertyAccess propertyAccess)
    {
        _classFactory = classFactory;
        _propertyAccess = propertyAccess;
    }

    /**
     * The LoggingDecorator service is used to decorate a service implementation so that it logs
     * method entry and exit (at level debug).
     */
    public LoggingDecorator build(@InjectService("ExceptionTracker")
    ExceptionTracker exceptionTracker)
    {
        return new LoggingDecoratorImpl(_classFactory, exceptionTracker);
    }

    /**
     * Provides access to additional service lifecycles. Two lifecycles are built in ("singleton"
     * and "primitive") but additional ones are accessed via this service (and its mapped
     * configuration).
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

    /** Contributes the "perthread" service lifecycle. */
    public void contributeServiceLifecycleSource(
            MappedConfiguration<String, ServiceLifecycle> configuration,
            @InjectService("ThreadCleanupHub")
            ThreadCleanupHub threadCleanupHub)
    {
        configuration.add("perthread", new PerThreadServiceLifecycle(threadCleanupHub,
                _classFactory));
    }

    /**
     * A service that implements the chain of command pattern, creating an efficient implementation
     * of a chain of command for an arbitrary interface.
     */
    public ChainBuilder buildChainBuilder()
    {
        return new ChainBuilderImpl(_classFactory);
    }

    /**
     * A service that implements the strategy pattern, around a {@link StrategyRegistry}.
     */
    public StrategyBuilder buildStrategyBuilder()
    {
        return new StrategyBuilderImpl(_classFactory);
    }

    /**
     * Services that provides read/write access to JavaBean properties. Encapsulates JavaBean
     * introspection, including serializing access to the non-thread-safe Introspector object.
     */
    public static PropertyAccess buildPropertyAccess()
    {
        return new PropertyAccessImpl();
    }

    /**
     * Builder that creates a shadow, a projection of a property of some other object.
     */
    public PropertyShadowBuilder buildPropertyShadowBuilder()
    {
        return new PropertyShadowBuilderImpl(_classFactory, _propertyAccess);
    }

    /**
     * Builder that creates a filter pipeline around a simple service interface.
     */
    public PipelineBuilder build(@InjectService("DefaultImplementationBuilder")
    DefaultImplementationBuilder builder)
    {
        return new PipelineBuilderImpl(_classFactory, builder);
    }

    /**
     * Builder that creates a default implementation of an interface.
     */
    public DefaultImplementationBuilder buildDefaultImplementationBuilder()
    {
        return new DefaultImplementationBuilderImpl(_classFactory);
    }

    /**
     * The master {@link ObjectProvider} is responsible for identifying a particular ObjectProvider
     * by its prefix, and delegating to that instance.
     * 
     * @param configuration
     *            map of ObjectProviders, keyed on prefix
     */
    public static ObjectProvider buildMasterObjectProvider(
            Map<String, ObjectProvider> configuration, @InjectService("SymbolSource")
            SymbolSource symbolSource, @InjectService("TypeCoercer")
            TypeCoercer typeCoercer)
    {
        return new MasterObjectProvider(configuration, symbolSource, typeCoercer);
    }

    /**
     * Contributes the "service:" object provider.
     */
    public static void contributeMasterObjectProvider(
            MappedConfiguration<String, ObjectProvider> configuration)
    {
        configuration.add("service", new ServiceObjectProvider());
    }

    /** Used by the {@link org.apache.tapestry.ioc.services.LoggingDecorator} service. */
    @Lifecycle("perthread")
    public static ExceptionTracker buildExceptionTracker()
    {
        return new ExceptionTrackerImpl();
    }

    public ExceptionAnalyzer buildExceptionAnalyzer()
    {
        return new ExceptionAnalyzerImpl(_propertyAccess);
    }

    /**
     * Returns the service that can coerce between different types.
     */
    public static TypeCoercer build(Collection<CoercionTuple> configuration)
    {
        return new TypeCoercerImpl(configuration);
    }

    /**
     * Contributes a set of standard type coercions:
     * <ul>
     * <li>Object to String</li>
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
     * <li>Long to Boolean (true if long value is non zero)</li>
     * <li>Null to Boolean (always false)</li>
     * <li>Null to String (still null)</li>
     * <li>Collection to Boolean (false if empty)</li>
     * <li>Object[] to List</li>
     * <li>Object to List (by wrapping as a singleton list)</li>
     * <li>Null to List (still null)</li>
     * </ul>
     * 
     * @see #buildTypeCoercer(Collection, ComponentInstantiatorSource)
     */

    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
        add(configuration, Object.class, String.class, new Coercion<Object, String>()
        {
            public String coerce(Object input)
            {
                return input.toString();
            }
        });

        // This is necessary, otherwise we get a failure because void --> Object : Object --> String
        // throws an NPE

        add(configuration, void.class, String.class, new Coercion<Void, String>()
        {
            public String coerce(Void input)
            {
                return null;
            }
        });

        // This keeps a null -> List from being null -> Object : Object -> List (i.e., an empty List
        // of a single null).

        add(configuration, void.class, List.class, new Coercion<Void, List>()
        {
            public List coerce(Void input)
            {
                return null;
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
    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType,
            Class<T> targetType, Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = new CoercionTuple<S, T>(sourceType, targetType, coercion);

        configuration.add(tuple);
    }

    @Lifecycle("perthread")
    public static ThreadLocale buildThreadLocale()
    {
        return new ThreadLocaleImpl();
    }

    public static SymbolSource build(List<SymbolProvider> configuration)
    {
        return new SymbolSourceImpl(configuration);
    }

    public static void contributeSymbolSource(OrderedConfiguration<SymbolProvider> configuration,
            @InjectService("ApplicationDefaults")
            SymbolProvider applicationDefaults, @InjectService("FactoryDefaults")
            SymbolProvider factoryDefaults)
    {
        configuration.add("SystemProperties", new SystemPropertiesSymbolProvider());
        configuration.add("ApplicationDefaults", applicationDefaults, "after:SystemProperties");
        configuration.add("FactoryDefaults", factoryDefaults, "after:ApplicationDefaults");
    }

    public static SymbolProvider buildApplicationDefaults(Map<String, String> configuration)
    {
        return new MapSymbolProvider(configuration);
    }

    public static SymbolProvider buildFactoryDefaults(Map<String, String> configuration)
    {
        return new MapSymbolProvider(configuration);
    }
}
