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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Functional operations on collections with generics support. The core interface is {@link Flow} to
 * which operations
 * and transformations
 * (in terms of {@link Predicate}s, {@link Mapper}s and {@link Reducer}s) to create new Flows. Flows
 * are initially
 * created
 * using {@link #flow(Collection)} and {@link #flow(Object...)}.
 *
 * F will be used a bit, thus it has a short name (for those who don't like static imports). It provides a base set of
 * Predicate, Mapper and Reducer factories. A good development pattern for applications is to provide a similar,
 * application-specific, set of such factories.
 *
 * @since 5.2.0
 */
@SuppressWarnings("all")
public class F
{
    final static Flow<?> EMPTY_FLOW = new EmptyFlow();

    @SuppressWarnings("unchecked")
    static <T> Flow<T> emptyFlow()
    {
        return (Flow<T>) EMPTY_FLOW;
    }

    /**
     * A Predicate factory for equality of an element from a flow against a specified
     * value.
     */
    public static <T> Predicate<T> eql(final T value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return element.equals(value);
            }
        };
    }

    /**
     * Predicate that returns true if the provided string is blank (null or all whitespace).
     */
    public static Predicate<String> IS_BLANK = new Predicate<String>()
    {
        @Override
        public boolean accept(String element)
        {
            return element == null || element.trim().length() == 0;
        }
    };

    /**
     * A Predicate factory for comparison of a Comparable element from a flow against a fixed value.
     */
    public static <T extends Comparable<T>> Predicate<T> eq(final T value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return element.compareTo(value) == 0;
            }
        };
    }

    /**
     * A Predicate factory for comparison of a Comparable element against a fixed value.
     */
    public static <T extends Comparable<T>> Predicate<T> neq(final T value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T object)
            {
                return object.compareTo(value) != 0;
            }
        };
    }

    /**
     * A Predicate factory for comparison of a Comparable against a fixed value; true
     * if the flow element is greater than the provided value.
     */
    public static <T extends Comparable<T>> Predicate<T> gt(final T value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return element.compareTo(value) > 0;
            }
        };
    }

    /**
     * A Predicate factory for comparison of a Comparable against a fixed value; true
     * if the flow element is greater than or equal to the value.
     */
    public static <T extends Comparable<T>> Predicate<T> gteq(final T value)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return element.compareTo(value) >= 0;
            }
        };
    }

    /**
     * A Predicate factory for comparison of a Comparable against a fixed value; true
     * if the element is less than the value.
     */
    public static <T extends Comparable<T>> Predicate<T> lt(T value)
    {
        return not(gteq(value));
    }

    /**
     * A Predicate factory for comparison of a Comprable element against a fixed value; true
     * if the element is less than or equal to the value.
     */
    public static <T extends Comparable<T>> Predicate<T> lteq(T value)
    {
        return not(gt(value));
    }

    /**
     * A Predicate factory; returns true if the value from the Flow is null.
     */
    public static <T> Predicate<T> isNull()
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return element == null;
            }
        };
    }

    /**
     * A Predicate factory; returns true if the value from the Flow is not null.
     */
    public static <T> Predicate<T> notNull()
    {
        return not(isNull());
    }

    /**
     * A Mapper factory that gets the string value of the flow value using {@link String#valueOf(Object)}.
     */
    public static <T> Mapper<T, String> stringValueOf()
    {
        return new Mapper<T, String>()
        {
            @Override
            public String map(T value)
            {
                return String.valueOf(value);
            }
        };
    }

    /**
     * A Mapper factory; the returned Mapper ignores its input value and always returns a
     * predetermined result.
     */
    public static <S, T> Mapper<S, T> always(final T fixedResult)
    {
        return new Mapper<S, T>()
        {
            @Override
            public T map(S input)
            {
                return fixedResult;
            }
        };
    }

    /**
     * A Mapper factory that combines a Predicate with two {@link Mapper}s; evaluating the predicate
     * selects one of the two mappers.
     *
     * @param predicate
     *         evaluated to selected a coercion
     * @param ifAccepted
     *         used when predicate evaluates to true
     * @param ifRejected
     *         used when predicate evaluates to false
     */
    public static <S, T> Mapper<S, T> select(final Predicate<? super S> predicate, final Mapper<S, T> ifAccepted,
                                             final Mapper<S, T> ifRejected)
    {
        assert predicate != null;
        assert ifAccepted != null;
        assert ifRejected != null;

        return new Mapper<S, T>()
        {
            @Override
            public T map(S input)
            {
                Mapper<S, T> active = predicate.accept(input) ? ifAccepted : ifRejected;

                return active.map(input);
            }
        };
    }

    /**
     * Override of {@link #select(Predicate, Mapper, Mapper)} where rejected values are replaced
     * with null.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted)
    {
        return select(predicate, ifAccepted, (T) null);
    }

    /**
     * Override of {@link #select(Predicate, Mapper)} where rejected values are replaced with a
     * fixed value.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted, T ifRejectedValue)
    {
        Mapper<S, T> rejectedMapper = always(ifRejectedValue);

        return select(predicate, ifAccepted, rejectedMapper);
    }

    /**
     * A Mapper factory; the Mapper returns the the flow value unchanged.
     */
    public static <S> Mapper<S, S> identity()
    {
        return new Mapper<S, S>()
        {
            @Override
            public S map(S input)
            {
                return input;
            }
        };
    }

    /**
     * Allows a Mapper that maps to boolean to be used as a Predicate.
     */
    public static <S> Predicate<S> toPredicate(final Mapper<S, Boolean> mapper)
    {
        assert mapper != null;

        return new Predicate<S>()
        {
            @Override
            public boolean accept(S object)
            {
                return mapper.map(object);
            }
        };
    }

    /**
     * A Reducer that operates on a Flow of Integers and is used to sum the values.
     */
    public static Reducer<Integer, Integer> SUM_INTS = new Reducer<Integer, Integer>()
    {
        @Override
        public Integer reduce(Integer accumulator, Integer value)
        {
            return accumulator + value;
        }
    };

    /**
     * A two-input Mapper used to add the values from two Flows of Integers into a Flow of Integer
     * sums.
     */
    public static Mapper2<Integer, Integer, Integer> ADD_INTS = new Mapper2<Integer, Integer, Integer>()
    {
        @Override
        public Integer map(Integer first, Integer second)
        {
            return first + second;
        }
    };

    /**
     * Extracts the values from the collection to form a {@link Flow}. The Collection
     * may change after the Flow is created without affecting the Flow.
     */
    public static <T> Flow<T> flow(Collection<T> values)
    {
        assert values != null;

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

    /**
     * Creates a lazy Flow from the {@link Iterator} obtained from the iterable. The Flow
     * will be threadsafe as long as the iterable yields a new Iterator on each invocation <em>and</em> the underlying
     * iterable object is not modified while the Flow is evaluating.
     * In other words, not extremely threadsafe.
     */
    public static <T> Flow<T> flow(Iterable<T> iterable)
    {
        assert iterable != null;

        return flow(iterable.iterator());
    }

    /**
     * Creates a lazy Flow from the {@link Iterator}. The Flow will be threadsafe as long as the underlying iterable
     * object is not modified while the Flow is evaluating. In other words, not extremely threadsafe.
     *
     * @since 5.3
     */
    public static <T> Flow<T> flow(Iterator<T> iterator)
    {
        return lazy(new LazyIterator<T>(iterator));
    }

    /**
     * Creates a ZippedFlow from the provided map; the order of the tuples in the ZippedFlow is defined
     * by the iteration order of the map entries.
     *
     * @param <A>
     *         type of key and first tuple value
     * @param <B>
     *         type of value and second tuple value
     * @param map
     *         source of tuples
     * @return zipped flow created from map
     * @since 5.3
     */
    public static <A, B> ZippedFlow<A, B> zippedFlow(Map<A, B> map)
    {
        assert map != null;

        Flow<Tuple<A, B>> tuples = F.flow(map.entrySet()).map(new Mapper<Map.Entry<A, B>, Tuple<A, B>>()
        {
            @Override
            public Tuple<A, B> map(Entry<A, B> element)
            {
                return Tuple.create(element.getKey(), element.getValue());
            }
        });

        return ZippedFlowImpl.create(tuples);
    }

    /**
     * Creates a lazy Flow that returns integers in the given range. The range starts
     * with the lower value and counts by 1 up to the upper range (which is not part of
     * the Flow). If lower equals upper, the Flow is empty. If upper is less than lower,
     * the Flow counts down instead.
     *
     * @param lower
     *         start of range (inclusive)
     * @param upper
     *         end of range (exclusive)
     */
    public static Flow<Integer> range(int lower, int upper)
    {
        if (lower == upper)
            return F.emptyFlow();

        if (lower < upper)
            return lazy(new LazyRange(lower, upper, 1));

        return lazy(new LazyRange(lower, upper, -1));
    }

    /**
     * Creates a {@link Flow} from a {@linkplain LazyFunction lazy function}.
     */
    public static <T> Flow<T> lazy(LazyFunction<T> function)
    {
        assert function != null;

        return new LazyFlow<T>(function);
    }

    private static <T> LazyFunction<T> toLazyFunction(final T currentValue, final Mapper<T, T> function)
    {
        return new LazyFunction<T>()
        {
            @Override
            public LazyContinuation<T> next()
            {
                final T nextValue = function.map(currentValue);

                return new LazyContinuation<T>(nextValue, toLazyFunction(nextValue, function));
            }
        };
    }

    /**
     * Creates an infinite lazy flow from an initial value and a function to map from the current value to the
     * next value.
     *
     * @param initial
     *         initial value in flow
     * @param function
     *         maps from current value in flow to next value in flow
     * @return lazy flow
     */
    public static <T> Flow<T> iterate(final T initial, final Mapper<T, T> function)
    {
        LazyFunction<T> head = new LazyFunction<T>()
        {
            @Override
            public LazyContinuation<T> next()
            {
                return new LazyContinuation<T>(initial, toLazyFunction(initial, function));
            }
        };

        return lazy(head);
    }

    /**
     * Creates an <em>infinite</em> series of numbers.
     *
     * Attempting to get the {@linkplain Flow#count()} of the series will form an infinite loop.
     */
    public static Flow<Integer> series(int start, final int delta)
    {
        return iterate(start, new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer element)
            {
                return element + delta;
            }
        });
    }

    /**
     * A Worker factory; the returnedWorker adds the values to a provided collection.
     */
    public static <T> Worker<T> addToCollection(final Collection<T> coll)
    {
        return new Worker<T>()
        {
            @Override
            public void work(T value)
            {
                coll.add(value);
            }
        };
    }

    /**
     * A Predicate factory for matching String elements with a given prefix.
     *
     * @since 5.3
     */
    public static Predicate<String> startsWith(String prefix)
    {
        return startsWith(prefix, false);
    }

    /**
     * As {@link #startsWith(String)}, but ignores case.
     *
     * @since 5.3
     */
    public static Predicate<String> startsWithIgnoringCase(String prefix)
    {
        return startsWith(prefix, true);
    }

    /**
     * @since 5.3
     */
    private static Predicate<String> startsWith(final String prefix, final boolean ignoreCase)
    {
        return new Predicate<String>()
        {
            @Override
            public boolean accept(String element)
            {
                return element.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
            }
        };
    }

    /**
     * A Predicate factory for matching String elements with a given suffix.
     *
     * @since 5.3
     */
    public static Predicate<String> endsWith(String suffix)
    {
        return endsWith(suffix, false);
    }

    /**
     * As with {@link #endsWith(String)} but ignores case.
     *
     * @since 5.3
     */
    public static Predicate<String> endsWithIgnoringCase(String suffix)
    {
        return endsWith(suffix, true);
    }

    /**
     * @since 5.3
     */
    private static Predicate<String> endsWith(final String suffix, final boolean ignoreCase)
    {
        return new Predicate<String>()
        {
            @Override
            public boolean accept(String element)
            {
                return element
                        .regionMatches(ignoreCase, element.length() - suffix.length(), suffix, 0, suffix.length());
            }
        };
    }

    /**
     * Creates a Comparator for the Tuples of a {@link ZippedFlow} that sorts the Tuple elements based on the first
     * value in the Tuple.
     *
     * @since 5.3
     */
    public static <A extends Comparable<A>, B> Comparator<Tuple<A, B>> orderByFirst()
    {
        return new Comparator<Tuple<A, B>>()
        {
            @Override
            public int compare(Tuple<A, B> o1, Tuple<A, B> o2)
            {
                return o1.first.compareTo(o2.first);
            }
        };
    }

    /**
     * Creates a Comparator for the Tuples of a {@link ZippedFlow} that sorts the Tuple elements based on the first
     * value in the Tuple.
     *
     * @since 5.3
     */
    public static <A, B extends Comparable<B>> Comparator<Tuple<A, B>> orderBySecond()
    {
        return new Comparator<Tuple<A, B>>()
        {
            @Override
            public int compare(Tuple<A, B> o1, Tuple<A, B> o2)
            {
                return o1.second.compareTo(o2.second);
            }
        };
    }

    /**
     * Inverts a predicate.
     *
     * @param delegate
     *         the predicate to invert
     * @return a new predicate that is inverse to the existing predicate
     * @since 5.3
     */
    public static <T> Predicate<T> not(final Predicate<? super T> delegate)
    {
        assert delegate != null;

        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                return !delegate.accept(element);
            }
        };
    }

    /**
     * Combines two mappers into a composite mapping from type A to type C via type B.
     *
     * @param abMapper
     *         maps from A to B
     * @param bcMapper
     *         maps from B to C
     * @return mapper from A to C
     */
    public static <A, B, C> Mapper<A, C> combine(final Mapper<A, B> abMapper, final Mapper<B, C> bcMapper)
    {
        assert abMapper != null;
        assert bcMapper != null;

        return new Mapper<A, C>()
        {

            @Override
            public C map(A aElement)
            {
                B bElement = abMapper.map(aElement);

                return bcMapper.map(bElement);
            }

        };
    }

    /**
     * Combines any number of delegates as a logical and operation. Evaluation terminates
     * with the first delegate predicate that returns false.
     *
     * @param delegates
     *         to evaluate
     * @return combined delegate
     * @since 5.3
     */
    public static <T> Predicate<T> and(final Predicate<? super T>... delegates)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                for (Predicate<? super T> delegate : delegates)
                {
                    if (!delegate.accept(element))
                        return false;
                }

                return true;
            }
        };
    }

    /**
     * Combines any number of delegates as a logical or operation. Evaluation terminates
     * with the first delegate predicate that returns true.
     *
     * @param delegates
     *         to evaluate
     * @return combined delegate
     * @since 5.3
     */
    public static <T> Predicate<T> or(final Predicate<? super T>... delegates)
    {
        return new Predicate<T>()
        {
            @Override
            public boolean accept(T element)
            {
                for (Predicate<? super T> delegate : delegates)
                {
                    if (delegate.accept(element))
                        return true;
                }

                return false;
            }
        };
    }

    /**
     * Combines several compatible workers together into a composite.
     *
     * @since 5.3
     */
    public static <T> Worker<T> combine(final Worker<? super T>... delegates)
    {
        assert delegates.length > 0;

        return new Worker<T>()
        {
            @Override
            public void work(T value)
            {
                for (Worker<? super T> delegate : delegates)
                {
                    delegate.work(value);
                }
            }
        };
    }
}
