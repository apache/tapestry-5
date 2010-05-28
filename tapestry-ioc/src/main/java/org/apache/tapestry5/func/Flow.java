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

import java.util.List;

/**
 * A fluent interface for manipulating collections using map, reduce, filter and remove. Flows are
 * immutable; operations create new flows to replace the old ones.
 * <p>
 * A future enhancement may be to make flows lazy.
 * 
 * @since 5.2.0
 */
public interface Flow<T>
{
    /** Maps a Flow into a new Flow with different type values. */
    <X> Flow<X> map(Mapper<T, X> mapper);

    /**
     * Filters values, keeping only values where the predicate is true, returning a new Flow with just
     * the remaining values.
     */
    Flow<T> filter(Predicate<? super T> predicate);

    /** Removes values where the predicate returns true, returning a new Flow with just the remaining values. */
    Flow<T> remove(Predicate<? super T> predicate);

    /**
     * Applies a Reducer to the values of the Flow. The Reducer is passed the initial value
     * and the first value from the flow. The result is captured as the accumulator and passed
     * to the Reducer with the next value from the flow, and so on. The final accumulator
     * value is returned. If the flow is empty, the initial value is returned.
     */
    <A> A reduce(Reducer<A, T> reducer, A initial);

    /**
     * Applies the worker to each value in the Flow, then returns the flow for further behaviors.
     */
    Flow<T> each(Worker<? super T> worker);

    /** Converts the Flow into an unmodifiable list of values. */
    List<T> toList();

    /** Returns a new flow with the same elements but in reverse order. */
    Flow<T> reverse();

    /** Returns true if the Flow contains no values. */
    boolean isEmpty();

    /** Returns a new Flow with the other Flow's elements appended to this Flow's. */
    Flow<T> concat(Flow<? extends T> other);

    /** Returns a new Flow with the values in the list appended to this Flow. */
    Flow<T> concat(List<? extends T> list);

    <V extends T> Flow<T> append(V... values);
}
