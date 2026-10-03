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

package org.apache.tapestry5.commons.benchmarks;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.internal.BasicTypeCoercions;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.util.TimeInterval;
import org.apache.tapestry5.func.Flow;

/**
 * A lambda-for-lambda translation of
 * {@link BasicTypeCoercions#provideBasicTypeCoercions(MappedConfiguration)}, which contributes
 * 27 coercions as anonymous inner classes.
 *
 * The two are behaviourally identical; what differs is how the JVM materialises the
 * {@link Coercion} instances. The anonymous version loads one class file per coercion. This
 * version emits one {@code invokedynamic} call site per coercion, each of which spins a hidden
 * class through {@code LambdaMetafactory} the first time it is reached.
 *
 * {@link CoercionStyleBenchmark} measures the difference. Keep this file in sync with
 * {@code BasicTypeCoercions} when coercions are added or removed there, or the comparison
 * stops being apples to apples.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class LambdaTypeCoercions
{
    public static void provideBasicTypeCoercions(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration)
    {
        add(configuration, Object.class, String.class, Object::toString);

        add(configuration, Object.class, Boolean.class, (Object input) -> input != null);

        add(configuration, String.class, Double.class, Double::valueOf);

        // String to BigDecimal is important, as String->Double->BigDecimal would lose
        // precision.

        add(configuration, String.class, BigDecimal.class, BigDecimal::new);

        add(configuration, BigDecimal.class, Double.class, BigDecimal::doubleValue);

        add(configuration, String.class, BigInteger.class, BigInteger::new);

        add(configuration, String.class, Long.class, Long::valueOf);

        add(configuration, String.class, Integer.class, Integer::valueOf);

        add(configuration, Long.class, Byte.class, Long::byteValue);

        add(configuration, Long.class, Short.class, Long::shortValue);

        add(configuration, Long.class, Integer.class, Long::intValue);

        add(configuration, Number.class, Long.class, Number::longValue);

        add(configuration, Double.class, Float.class, Double::floatValue);

        add(configuration, Long.class, Double.class, Long::doubleValue);

        add(configuration, String.class, Boolean.class, (String input) ->
        {
            String trimmed = input == null ? "" : input.trim();

            if (trimmed.equalsIgnoreCase("false") || trimmed.length() == 0)
                return false;

            // Any non-blank string but "false"

            return true;
        });

        add(configuration, Number.class, Boolean.class, (Number input) -> input.longValue() != 0);

        add(configuration, Void.class, Boolean.class, (Void input) -> false);

        add(configuration, Collection.class, Boolean.class, (Collection input) -> !input.isEmpty());

        add(configuration, Object.class, List.class, (Object input) -> Collections.singletonList(input));

        add(configuration, Object[].class, List.class, (Object[] input) -> Arrays.asList(input));

        add(configuration, Object[].class, Boolean.class, (Object[] input) -> input != null && input.length > 0);

        add(configuration, Float.class, Double.class, Float::doubleValue);

        // Shared by the eight primitive array types below, exactly as in BasicTypeCoercions:
        // one implementation, eight contributions.
        Coercion primitiveArrayCoercion = (Coercion<Object, List>) (Object input) ->
        {
            int length = Array.getLength(input);
            Object[] array = new Object[length];
            for (int i = 0; i < length; i++)
            {
                array[i] = Array.get(input, i);
            }
            return Arrays.asList(array);
        };

        add(configuration, byte[].class, List.class, primitiveArrayCoercion);
        add(configuration, short[].class, List.class, primitiveArrayCoercion);
        add(configuration, int[].class, List.class, primitiveArrayCoercion);
        add(configuration, long[].class, List.class, primitiveArrayCoercion);
        add(configuration, float[].class, List.class, primitiveArrayCoercion);
        add(configuration, double[].class, List.class, primitiveArrayCoercion);
        add(configuration, char[].class, List.class, primitiveArrayCoercion);
        add(configuration, boolean[].class, List.class, primitiveArrayCoercion);

        add(configuration, String.class, File.class, File::new);

        add(configuration, String.class, TimeInterval.class, TimeInterval::new);

        add(configuration, TimeInterval.class, Long.class, TimeInterval::milliseconds);

        add(configuration, Object.class, Object[].class, (Object input) -> new Object[]{ input });

        add(configuration, Collection.class, Object[].class, (Collection input) -> input.toArray());

        CoercionTuple<Flow, List> flowToListCoercion = CoercionTuple.create(Flow.class, List.class, Flow::toList);
        configuration.add(flowToListCoercion.getKey(), flowToListCoercion);

        CoercionTuple<Flow, Boolean> flowToBooleanCoercion = CoercionTuple.create(Flow.class, Boolean.class, (i) -> !i.isEmpty());
        configuration.add(flowToBooleanCoercion.getKey(), flowToBooleanCoercion);
    }

    private static <S, T> void add(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration, Class<S> sourceType,
                                   Class<T> targetType, Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = CoercionTuple.create(sourceType, targetType, coercion);
        configuration.add(tuple.getKey(), tuple);
    }
}
