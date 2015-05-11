// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.internal;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.util.TimeInterval;

/**
 * Class that provides Tapestry-IoC's basic type coercions.
 * @see TypeCoercer
 * @see Coercion
 */
public class BasicTypeCoercions
{
    /**
     * Provides the basic type coercions to a {@link Configuration} instance. 
     */
    public static void provideBasicTypeCoercions(Configuration<CoercionTuple> configuration)
    {
        add(configuration, Object.class, String.class, new Coercion<Object, String>()
        {
            @Override
            public String coerce(Object input)
            {
                return input.toString();
            }
        });

        add(configuration, Object.class, Boolean.class, new Coercion<Object, Boolean>()
        {
            @Override
            public Boolean coerce(Object input)
            {
                return input != null;
            }
        });

        add(configuration, String.class, Double.class, new Coercion<String, Double>()
        {
            @Override
            public Double coerce(String input)
            {
                return Double.valueOf(input);
            }
        });

        // String to BigDecimal is important, as String->Double->BigDecimal would lose
        // precision.

        add(configuration, String.class, BigDecimal.class, new Coercion<String, BigDecimal>()
        {
            @Override
            public BigDecimal coerce(String input)
            {
                return new BigDecimal(input);
            }
        });

        add(configuration, BigDecimal.class, Double.class, new Coercion<BigDecimal, Double>()
        {
            @Override
            public Double coerce(BigDecimal input)
            {
                return input.doubleValue();
            }
        });

        add(configuration, String.class, BigInteger.class, new Coercion<String, BigInteger>()
        {
            @Override
            public BigInteger coerce(String input)
            {
                return new BigInteger(input);
            }
        });

        add(configuration, String.class, Long.class, new Coercion<String, Long>()
        {
            @Override
            public Long coerce(String input)
            {
                return Long.valueOf(input);
            }
        });

        add(configuration, Long.class, Byte.class, new Coercion<Long, Byte>()
        {
            @Override
            public Byte coerce(Long input)
            {
                return input.byteValue();
            }
        });

        add(configuration, Long.class, Short.class, new Coercion<Long, Short>()
        {
            @Override
            public Short coerce(Long input)
            {
                return input.shortValue();
            }
        });

        add(configuration, Long.class, Integer.class, new Coercion<Long, Integer>()
        {
            @Override
            public Integer coerce(Long input)
            {
                return input.intValue();
            }
        });

        add(configuration, Number.class, Long.class, new Coercion<Number, Long>()
        {
            @Override
            public Long coerce(Number input)
            {
                return input.longValue();
            }
        });

        add(configuration, Double.class, Float.class, new Coercion<Double, Float>()
        {
            @Override
            public Float coerce(Double input)
            {
                return input.floatValue();
            }
        });

        add(configuration, Long.class, Double.class, new Coercion<Long, Double>()
        {
            @Override
            public Double coerce(Long input)
            {
                return input.doubleValue();
            }
        });

        add(configuration, String.class, Boolean.class, new Coercion<String, Boolean>()
        {
            @Override
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
            @Override
            public Boolean coerce(Number input)
            {
                return input.longValue() != 0;
            }
        });

        add(configuration, Void.class, Boolean.class, new Coercion<Void, Boolean>()
        {
            @Override
            public Boolean coerce(Void input)
            {
                return false;
            }
        });

        add(configuration, Collection.class, Boolean.class, new Coercion<Collection, Boolean>()
        {
            @Override
            public Boolean coerce(Collection input)
            {
                return !input.isEmpty();
            }
        });

        add(configuration, Object.class, List.class, new Coercion<Object, List>()
        {
            @Override
            public List coerce(Object input)
            {
                return Collections.singletonList(input);
            }
        });

        add(configuration, Object[].class, List.class, new Coercion<Object[], List>()
        {
            @Override
            public List coerce(Object[] input)
            {
                return Arrays.asList(input);
            }
        });

        add(configuration, Object[].class, Boolean.class, new Coercion<Object[], Boolean>()
        {
            @Override
            public Boolean coerce(Object[] input)
            {
                return input != null && input.length > 0;
            }
        });

        add(configuration, Float.class, Double.class, new Coercion<Float, Double>()
        {
            @Override
            public Double coerce(Float input)
            {
                return input.doubleValue();
            }
        });

        Coercion primitiveArrayCoercion = new Coercion<Object, List>()
        {
            @Override
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
            @Override
            public File coerce(String input)
            {
                return new File(input);
            }
        });

        add(configuration, String.class, TimeInterval.class, new Coercion<String, TimeInterval>()
        {
            @Override
            public TimeInterval coerce(String input)
            {
                return new TimeInterval(input);
            }
        });

        add(configuration, TimeInterval.class, Long.class, new Coercion<TimeInterval, Long>()
        {
            @Override
            public Long coerce(TimeInterval input)
            {
                return input.milliseconds();
            }
        });

        add(configuration, Object.class, Object[].class, new Coercion<Object, Object[]>()
        {
            @Override
            public Object[] coerce(Object input)
            {
                return new Object[]
                        {input};
            }
        });

        add(configuration, Collection.class, Object[].class, new Coercion<Collection, Object[]>()
        {
            @Override
            public Object[] coerce(Collection input)
            {
                return input.toArray();
            }
        });
        
        configuration.add(CoercionTuple.create(Flow.class, List.class, new Coercion<Flow, List>()
        {
            @Override
            public List coerce(Flow input)
            {
                return input.toList();
            }
        }));

        configuration.add(CoercionTuple.create(Flow.class, Boolean.class, new Coercion<Flow, Boolean>()
        {
            @Override
            public Boolean coerce(Flow input)
            {
                return !input.isEmpty();
            }
        }));
        

    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType,
                                   Class<T> targetType, Coercion<S, T> coercion)
    {
        configuration.add(CoercionTuple.create(sourceType, targetType, coercion));
    }
    
    

}
