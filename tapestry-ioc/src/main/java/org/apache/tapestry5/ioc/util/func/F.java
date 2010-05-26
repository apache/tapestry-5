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

package org.apache.tapestry5.ioc.util.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.Coercion;

/**
 * Functional operations on collections with generics support. Tending to use the equivalent names from
 * Clojure. As with Clojure, all these functions return new lists. Unlike Clojure, there's no guarantee
 * that the lists are immutable, and there isn't built-in laziness.
 * 
 * @since 5.2.0
 */
public class F
{

    /**
     * Functional map (i.e., transform operation) from a Collection&lt;S&gt; to List&lt;T&gt;.
     */
    public static <S, T> List<T> map(Mapper<S, T> mapper, Collection<S> source)
    {
        Defense.notNull(source, "source");
        Defense.notNull(mapper, "mapper");

        if (source.isEmpty())
            return Collections.emptyList();

        List<T> result = new ArrayList<T>(source.size());

        for (S s : source)
        {
            T t = mapper.map(s);

            result.add(t);
        }

        return result;
    }

    public static <S, T> List<T> map(Mapper<S, T> mapper, S... source)
    {
        Defense.notNull(source, "source");

        return map(mapper, Arrays.asList(source));
    }

    public static <S, T> Mapper<S, T> toMapper(final Coercion<S, T> coercion)
    {
        Defense.notNull(coercion, "coercion");

        return new AbstractMapper<S, T>()
        {

            public T map(S value)
            {
                return coercion.coerce(value);
            }
        };
    }

    /**
     * Performs an operation on each element of the source collection.
     */
    public static <T> void each(Worker<T> operation, Collection<T> source)
    {
        for (T t : source)
        {
            operation.work(t);
        }
    }

    /**
     * Performs an operation on each of the values.
     */
    public static <T> void each(Worker<T> operation, T... values)
    {
        for (T t : values)
        {
            operation.work(t);
        }
    }

    /** Returns a new list containing only those elements for which the predicate evaluates to true. */
    public static <T> List<T> filter(Predicate<? super T> predicate, List<T> source)
    {
        Defense.notNull(source, "source");
        Defense.notNull(predicate, "predicate");

        if (source.isEmpty())
            return Collections.emptyList();

        List<T> result = new ArrayList<T>(source.size());

        for (T item : source)
        {
            if (predicate.accept(item))
                result.add(item);
        }

        return result;
    }

    /**
     * Returns a new list containing only those values of the source list for which the predicate
     * evaluates to false.
     */
    public static <T> List<T> remove(Predicate<? super T> predicate, List<T> source)
    {
        Defense.notNull(predicate, "predicate");

        return filter(predicate.invert(), source);
    }

    public static Predicate<Number> eq(final long value)
    {
        return new AbstractPredicate<Number>()
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
        return new AbstractPredicate<Number>()
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
        return new AbstractPredicate<T>()
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

    public static <S, T> Mapper<S, T> always(final T fixedResult)
    {
        return new AbstractMapper<S, T>()
        {
            public T map(S input)
            {
                return fixedResult;
            }
        };
    }

    /**
     * Coercion factory that combines a Predicate with two {@link Mapper}s; evaluating the predicate selects one of the
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
        return new AbstractMapper<S, T>()
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

    public static <S> Mapper<S, S> identity()
    {
        return new AbstractMapper<S, S>()
        {
            public S map(S input)
            {
                return input;
            }
        };
    }

    /** Allows Coercion to boolean to be used as a Predicate. */
    public static <S> Predicate<S> toPredicate(final Mapper<S, Boolean> mapper)
    {
        return new AbstractPredicate<S>()
        {
            public boolean accept(S object)
            {
                return mapper.map(object);
            };
        };
    }
}
