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

import java.util.Comparator;
import java.util.List;

/**
 * A Flow is a a functional interface for working with an ordered collection of values.
 * A given Flow contains only values of a particular type. Standard operations allow for
 * filtering the Flow, or appending values to the Flow. Since Flows are immutable, all operations
 * on Flows return new immutable Flows. Flows are thread safe (to the extent that the {@link Mapper}s, {@link Predicate}
 * s, {@link Worker}s and {@link Reducer}s applied to the Flow are).
 * Flows are <em>lazy</em>: filtering, mapping, and concatenating Flows will do so with no, or a minimum, of evaluation.
 * However, converting a Flow into a {@link List} will force a realization of the entire Flow.
 * <p>
 * In some cases, a Flow may be an infinite, lazily evaluated sequence. Operations that iterate over all values (such as
 * {@link #count()} or {@link #reduce(Reducer, Object)}) may become infinite loops.
 * <p>
 * Using Flows allows for a very fluid interface.
 * <p>
 * Flows are initially created using {@link F#flow(java.util.Collection)} or {@link F#flow(Object...)}.
 * 
 * @since 5.2.0
 * @see F#lazy(LazyFunction)
 */
public interface Flow<T> extends Iterable<T>
{
    /** Maps a Flow into a new Flow with different type values. Mapping is a lazy operation. */
    <X> Flow<X> map(Mapper<T, X> mapper);

    /**
     * Combines two Flows using a two-parameter Mapper. Each value of
     * this Flow, and the corresponding value of the other flow are passed through the Mapper
     * to provide the values of the output Flow. The length of the result Flow is
     * the smaller of the lengths of the two input Flows. Mapping is a lazy operation.
     */
    <X, Y> Flow<Y> map(Mapper2<T, X, Y> mapper, Flow<? extends X> flow);

    /**
     * Given a {@link Mapper} that maps a T to a Flow<X>, this method will lazily concatenate
     * all the output flows into a single Flow<X>.
     */
    <X> Flow<X> mapcat(Mapper<T, Flow<X>> mapper);

    /**
     * Filters values, keeping only values where the predicate is true, returning a new Flow with just
     * the retained values.
     */
    Flow<T> filter(Predicate<? super T> predicate);

    /** Removes values where the predicate returns true, returning a new Flow with just the remaining values. */
    Flow<T> remove(Predicate<? super T> predicate);

    /**
     * Applies a Reducer to the values of the Flow. The Reducer is passed the initial value
     * and the first value from the Flow. The result is captured as the accumulator and passed
     * to the Reducer with the next value from the Flow, and so on. The final accumulator
     * value is returned. If the flow is empty, the initial value is returned.
     * <p>
     * Reducing is a non-lazy operation; it will fully realize the values of the Flow.
     */
    <A> A reduce(Reducer<A, T> reducer, A initial);

    /**
     * Applies the worker to each value in the Flow, then returns the flow for further behaviors.
     * <p>
     * Each is a non-lazy operation; it will fully realize the values of the Flow.
     */
    Flow<T> each(Worker<? super T> worker);

    /**
     * Converts the Flow into an unmodifiable list of values. This is a non-lazy operation that will fully realize
     * the values of the Flow.
     */
    List<T> toList();

    /**
     * Converts the Flow into an array of values (due to type erasure, you have to remind the Flow about the
     * type).
     */
    T[] toArray(Class<T> type);

    /** Returns a new flow with the same elements but in reverse order. */
    Flow<T> reverse();

    /** Returns true if the Flow contains no values. This <em>may</em> realize the first value in the Flow. */
    boolean isEmpty();

    /** Returns a new Flow with the other Flow's elements appended to this Flow's. This is a lazy operation. */
    Flow<T> concat(Flow<? extends T> other);

    /** Returns a new Flow with the values in the list appended to this Flow. This is a lazy operation. */
    Flow<T> concat(List<? extends T> list);

    /** Appends any number of type compatible values to the end of this Flow. This is a lazy operation. */
    <V extends T> Flow<T> append(V... values);

    /**
     * Sorts this Flow, forming a new Flow. This is a non-lazy operation; it will fully realize the values of the Flow.
     * 
     * @throws ClassCastException
     *             if type <T> does not extend {@link Comparable}
     */
    Flow<T> sort();

    /**
     * Sorts this Flow using the comparator, forming a new Flow. This is a non-lazy operation; it will fully realize the
     * values of the Flow.
     */
    Flow<T> sort(Comparator<? super T> comparator);

    /**
     * Returns the first value in the Flow. Returns null for empty flows, but remember that null is a valid
     * value within a flow, so use {@link #isEmpty() to determine if a flow is actually empty. The first value can be
     * realized without realizing the full Flow.
     */
    T first();

    /**
     * Returns a new Flow containing all but the first value in this Flow. If this Flow has only a single item,
     * or is empty, this will return an empty Flow.
     */
    Flow<T> rest();

    /**
     * Returns the number of values in the Flow. This forces the realization of much of the Flow (i.e., because
     * each value will need to be passed through any {@link Predicate}s).
     */
    int count();

    /**
     * Returns a new Flow containing just the first values from
     * this Flow.
     * 
     * @param length
     *            maximum number of values in the Flow
     */
    Flow<T> take(int length);

    /**
     * Returns a new Flow with the first values omitted.
     * 
     * @param length
     *            number of values to drop
     */
    Flow<T> drop(int length);
}
