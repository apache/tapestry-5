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
import java.util.List;
import java.util.Set;

/**
 * @param T
 *         the type of data in the flow
 * @param FT
 *         the type of flow (either {@code Flow<T>} or {@code ZippedFlow<Tuple<T, ?>})
 * @since 5.3
 */
public interface FlowOperations<T, FT> extends Iterable<T>
{
    /**
     * Filters values, keeping only values where the predicate is true, returning a new Flow with
     * just the retained values.
     */
    FT filter(Predicate<? super T> predicate);

    /**
     * Removes values where the predicate returns true, returning a new Flow with just the remaining
     * values.
     */
    FT remove(Predicate<? super T> predicate);

    /**
     * Applies the worker to each element in the Flow, then returns the flow for further behaviors.
     *
     * Each is a non-lazy operation; it will fully realize the values of the Flow.
     */
    FT each(Worker<? super T> worker);

    /**
     * Converts the Flow into an unmodifiable list of values. This is a non-lazy operation that will
     * fully realize the values of the Flow.
     */
    List<T> toList();

    /**
     * Converts the Flow into an unmodifiable set of values. This is a non-lazy operation that will
     * fully realize the values of the Flow.
     */
    Set<T> toSet();

    /**
     * Returns a new flow with the same elements but in reverse order.
     */
    FT reverse();

    /**
     * Returns true if the Flow contains no values. This <em>may</em> realize the first value in the
     * Flow.
     */
    boolean isEmpty();

    /**
     * Returns the first element in the Flow. Returns null for empty flows, but remember that null
     * is a valid element within a flow, so use {@link #isEmpty()} to determine if a flow is actually
     * empty. The first element can be realized without realizing the full Flow.
     */
    T first();

    /**
     * Returns a new Flow containing all but the first element in this flow. If this flow has only a
     * single element, or is empty, this will return an empty Flow.
     */
    FT rest();

    /**
     * Returns the number of values in this flow. This forces the realization of much of the flow
     * (i.e., because each value will need to be passed through any {@link Predicate}s).
     */
    int count();

    /**
     * Sorts this flow using the comparator, forming a new flow. This is a non-lazy operation; it
     * will fully realize the elements of the Flow.
     */
    FT sort(Comparator<T> comparator);

    /**
     * Returns a new flow containing just the first elements from this Flow.
     *
     * @param length
     *         maximum number of values in the Flow
     */
    FT take(int length);

    /**
     * Returns a new flow with the first elements omitted.
     *
     * @param length
     *         number of values to drop
     */
    FT drop(int length);

    /**
     * Returns a new Flow with the elements in the collection appended to this Flow. This is a lazy
     * operation.
     *
     * Note that the type of this method changed from {@code List} to {@link Collection} in Tapestry 5.4. This
     * is considered a compatible change.
     *
     * @param collection
     *         collection of elements to be appended
     */
    FT concat(Collection<? extends T> collection);

    /**
     * Applies a Reducer to the values of the Flow. The Reducer is passed the initial value
     * and the first element from the Flow. The result is captured as the accumulator and passed
     * to the Reducer with the next value from the Flow, and so on. The final accumulator
     * value is returned. If the flow is empty, the initial value is returned.
     *
     * Reducing is a non-lazy operation; it will fully realize the values of the Flow.
     */
    <A> A reduce(Reducer<A, T> reducer, A initial);

    /**
     * Removes null elements from the flow (null tuples from a ZippedFlow), leaving just the
     * non-null elements. This is a lazy operation.
     *
     * @since 5.3
     */
    FT removeNulls();
}
