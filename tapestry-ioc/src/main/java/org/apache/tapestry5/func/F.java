// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.func;

import java.util.Collection;

/**
 * Functional operations on collections with generics support. Tending to use the equivalent names from
 * Clojure. As with Clojure, all these functions return new lists.
 * 
 * @since 5.2.0
 */
public class F
{
    private final static Flow<?> EMPTY_FLOW = new EmptyFlow();

    @SuppressWarnings("unchecked")
    static <T> Flow<T> emptyFlow()
    {
        return (Flow<T>) EMPTY_FLOW;
    }

    public static Predicate<Number> eq(final long value)
    {
        return new Predicate<Number>()
        {
            public boolean accept(Number object)
            {
                return object.longValue() == value;
            }
        };
    }

    public static Predicate<Number> neq(long value)
    {
        return eq(value).invert();
    }

    public static Predicate<Number> gt(final long value)
    {
        return new Predicate<Number>()
        {
            public boolean accept(Number object)
            {
                return object.longValue() > value;
            }
        };
    }

    public static Predicate<Number> gteq(long value)
    {
        return eq(value).or(gt(value));
    }

    public static Predicate<Number> lt(long value)
    {
        return gteq(value).invert();
    }

    public static Predicate<Number> lteq(long value)
    {
        return gt(value).invert();
    }

    public static <T> Predicate<T> isNull()
    {
        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return object == null;
            }
        };
    }

    public static <T> Predicate<T> notNull()
    {
        Predicate<T> isNull = isNull();

        return isNull.invert();
    }

    /** Returns a Mapper that ignores its input value and always returns a predetermined result. */
    public static <S, T> Mapper<S, T> always(final T fixedResult)
    {
        return new Mapper<S, T>()
        {
            public T map(S input)
            {
                return fixedResult;
            }
        };
    }

    /**
     * Mapper factory that combines a Predicate with two {@link Mapper}s; evaluating the predicate selects one of the
     * two mappers.
     * 
     * @param predicate
     *            evaluated to selected a coercion
     * @param ifAccepted
     *            used when predicate evaluates to true
     * @param ifRejected
     *            used when predicate evaluates to false
     */
    public static <S, T> Mapper<S, T> select(final Predicate<? super S> predicate, final Mapper<S, T> ifAccepted,
            final Mapper<S, T> ifRejected)
    {
        return new Mapper<S, T>()
        {
            public T map(S input)
            {
                Mapper<S, T> active = predicate.accept(input) ? ifAccepted : ifRejected;

                return active.map(input);
            }
        };
    }

    /**
     * Override of {@link #select(Predicate, Mapper, Mapper)} where rejected values are replaced with null.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted)
    {
        return select(predicate, ifAccepted, (T) null);
    }

    /**
     * Override of {@link #select(Predicate, Mapper)} where rejected values are replaced with a fixed value.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted, T ifRejected)
    {
        Mapper<S, T> rejectedMapper = always(ifRejected);

        return select(predicate, ifAccepted, rejectedMapper);
    }

    /** The identity mapper simply returns the input unchanged. */
    public static <S> Mapper<S, S> identity()
    {
        return new Mapper<S, S>()
        {
            public S map(S input)
            {
                return input;
            }
        };
    }

    /** Allows Mapper that maps to boolean to be used as a Predicate. */
    public static <S> Predicate<S> toPredicate(final Mapper<S, Boolean> mapper)
    {
        return new Predicate<S>()
        {
            public boolean accept(S object)
            {
                return mapper.map(object);
            };
        };
    }

    public static Reducer<Integer, Integer> SUM_INTS = new Reducer<Integer, Integer>()
    {
        public Integer reduce(Integer accumulator, Integer value)
        {
            return accumulator + value;
        };
    };

    /**
     * Extracts the values from the collection to form a {@link Flow}. The Collection
     * may change after the Flow is created without affecting the Flow.
     */
    public static <T> Flow<T> flow(Collection<T> values)
    {
        if (values.isEmpty())
            return emptyFlow();

        return new ArrayFlow<T>(values);
    }

    /**
     * Creates a new Flow from the values. You should not change the values array
     * after invoking this method (i.e., no defensive copy of the values is made).
     */
    public static <T> Flow<T> flow(T... values)
    {
        if (values.length == 0)
            return emptyFlow();

        return new ArrayFlow<T>(values);
    }
}
