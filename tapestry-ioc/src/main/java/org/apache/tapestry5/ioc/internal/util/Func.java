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

package org.apache.tapestry5.ioc.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.Predicate;
import org.apache.tapestry5.ioc.services.Coercion;

/**
 * Functional operations on collections with generics support. Tending to use the equivalent names from
 * Clojure. As with Clojure, all these functions return new lists. Unlike Clojure, there's no guarantee
 * that the lists are immutable, and there isn't built-in laziness.
 * 
 * @since 5.2.0
 */
public class Func
{

    /**
     * Functional map (i.e., transform operation) from a Collection&lt;S&gt; to List&lt;T&gt;.
     */
    public static <S, T> List<T> map(Coercion<S, T> coercion, Collection<S> source)
    {
        Defense.notNull(source, "source");
        Defense.notNull(coercion, "coercion");

        if (source.isEmpty())
            return Collections.emptyList();

        List<T> result = new ArrayList<T>(source.size());

        for (S s : source)
        {
            T t = coercion.coerce(s);

            result.add(t);
        }

        return result;
    }

    public static <S, T1, T2> Coercion<S, T2> combine(final Coercion<S, T1> first, final Coercion<T1, T2> second)
    {
        Defense.notNull(first, "first");
        Defense.notNull(second, "second");

        return new Coercion<S, T2>()
        {
            public T2 coerce(S input)
            {
                T1 intermediate = first.coerce(input);

                return second.coerce(intermediate);
            }
        };
    }

    /**
     * Performs an operation on each element of the source collection.
     */
    public static <T> void each(Operation<T> operation, Collection<T> source)
    {
        for (T t : source)
        {
            operation.op(t);
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

    /** Inverts a predicate, returning a new predicate. */
    public static <T> Predicate<T> invert(final Predicate<T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        return new Predicate<T>()
        {
            public boolean accept(T value)
            {
                return !predicate.accept(value);
            }
        };
    }

    /**
     * Returns a new list containing only those values of the source list for which the predicate
     * evaluates to false.
     */
    public static <T> List<T> remove(Predicate<? super T> predicate, List<T> source)
    {
        return filter(invert(predicate), source);
    }

    public static <T> Predicate<T> and(final Predicate<? super T> left, final Predicate<? super T> right)
    {
        Defense.notNull(left, "left");
        Defense.notNull(right, "right");

        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return left.accept(object) && right.accept(object);
            };
        };
    }

    public static <T> Predicate<T> or(final Predicate<? super T> left, final Predicate<? super T> right)
    {
        Defense.notNull(left, "left");
        Defense.notNull(right, "right");

        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return left.accept(object) || right.accept(object);
            };
        };
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
        return invert(eq(value));
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
        return or(eq(value), gt(value));
    }

    public static Predicate<Number> lt(long value)
    {
        return invert(gteq(value));
    }

    public static Predicate<Number> lteq(long value)
    {
        return invert(gt(value));
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

        return invert(isNull);
    }

    public static <S, T> Coercion<S, T> always(final T fixedResult)
    {
        return new Coercion<S, T>()
        {
            public T coerce(S input)
            {
                return fixedResult;
            }
        };
    }

    /**
     * Coercion factory that combines a Predicate with two coercions; evaluating the predicate selects one of the
     * two coercions.
     * 
     * @param predicate
     *            evaluated to selected a coercion
     * @param ifAccepted
     *            used when predicate evaluates to true
     * @param ifRejected
     *            used when predicate evaluates to false
     */
    public static <S, T> Coercion<S, T> select(final Predicate<? super S> predicate, final Coercion<S, T> ifAccepted,
            final Coercion<S, T> ifRejected)
    {
        return new Coercion<S, T>()
        {
            public T coerce(S input)
            {
                Coercion<S, T> active = predicate.accept(input) ? ifAccepted : ifRejected;

                return active.coerce(input);
            }
        };
    }

    /**
     * Override of {@link #select(Predicate, Coercion, Coercion)} where rejected values are replaced with null.
     */
    public static <S, T> Coercion<S, T> select(Predicate<? super S> predicate, Coercion<S, T> ifAccepted)
    {
        return select(predicate, ifAccepted, (T) null);
    }

    /**
     * Override of {@link #select(Predicate, Coercion)} where rejected values are replaced with a fixed value.
     */
    public static <S, T> Coercion<S, T> select(Predicate<? super S> predicate, Coercion<S, T> ifAccepted, T ifRejected)
    {
        Coercion<S, T> rejectedCoercion = always(ifRejected);

        return select(predicate, ifAccepted, rejectedCoercion);
    }

    public static <S> Coercion<S, S> identity()
    {
        return new Coercion<S, S>()
        {
            public S coerce(S input)
            {
                return input;
            }
        };
    }

    /** Allows Coercion to boolean to be used as a Predicate. */
    public static <S> Predicate<S> toPredicate(final Coercion<S, Boolean> coercion)
    {
        return new Predicate<S>()
        {
            public boolean accept(S object)
            {
                return coercion.coerce(object);
            };
        };
    }
}
