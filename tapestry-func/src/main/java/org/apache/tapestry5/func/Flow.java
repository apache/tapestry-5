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
 * A flow is a a functional interface for working with an ordered collection of elements.
 * A given Flow contains only elements of a particular type. Standard operations allow for
 * filtering the flow, or appending elements to the Flow. Since flows are immutable, all operations
 * on flows return new immutable flows. Flows are thread safe (to the extent that the {@link Mapper} , {@link Predicate}
 * , {@link Worker} and {@link Reducer} objects applied to the flow are).
 * Flows are <em>lazy</em>: filtering, mapping, and concatenating flows will do so with no, or a
 * minimum, of evaluation. However, converting a Flow into a {@link List} (or other collection) will
 * force a realization of the entire flow.
 *
 * In some cases, a flow may be an infinite, lazily evaluated sequence. Operations that iterate over all elements (such
 * as {@link #count()} or {@link #reduce(Reducer, Object)}) may become infinite loops.
 *
 * Using flows allows for a very fluid interface.
 *
 * Flows are initially created using {@link F#flow(java.util.Collection)}, {@link F#flow(Object...)} or
 * {@link F#flow(Iterable)}.
 * 
 * @since 5.2.0
 * @see F#lazy(LazyFunction)
 */
public interface Flow<T> extends FlowOperations<T, Flow<T>>
{
    /** Maps a Flow into a new Flow with different type values. Mapping is a lazy operation. */
    <X> Flow<X> map(Mapper<T, X> mapper);

    /**
     * Combines two Flows using a two-parameter Mapper. Each element of
     * this Flow, and the corresponding element of the other flow are passed through the Mapper
     * to provide the elements of the output Flow. The length of the result Flow is
     * the smaller of the lengths of the two input Flows. Mapping is a lazy operation.
     */
    <X, Y> Flow<Y> map(Mapper2<T, X, Y> mapper, Flow<? extends X> flow);

    /**
     * Given a {@link Mapper} that maps a T to a {@code Flow<X>}, this method will lazily concatenate
     * all the output flows into a single {@code Flow<X>}.
     */
    <X> Flow<X> mapcat(Mapper<T, Flow<X>> mapper);

    /**
     * Converts the Flow into an array of values (due to type erasure, you have to remind the Flow
     * about the type).
     */
    T[] toArray(Class<T> type);

    /**
     * Returns a new Flow with the other Flow's elements appended to this Flow's. This is a lazy
     * operation.
     */
    Flow<T> concat(Flow<? extends T> other);

    /**
     * Appends any number of type compatible values to the end of this Flow. This is a lazy
     * operation.
     */
    <V extends T> Flow<T> append(V... values);

    /**
     * Sorts this Flow, forming a new Flow. This is a non-lazy operation; it will fully realize the
     * values of the Flow.
     * 
     * @throws ClassCastException
     *             if type T does not extend {@link Comparable}
     */
    Flow<T> sort();

    /**
     * Zips this Flow together with another flow to form a Flow of {@link Tuple}s. The resulting
     * flow is the length of the shorter of the two input flows. Zipping flows together is a lazy
     * operation.
     *
     * The elements of this flow become the {@linkplain Tuple#first} value in each Tuple, the elements of the other flow
     * become the {@linkplain Tuple#second} value in each Tuple.
     * 
     * @param <X>
     *            type of element stored in the other flow
     * @param otherFlow
     *            contains elements to match with elements in this flow
     * @return flow of tuples combining values from this flow with values form the other flow
     * @since 5.3
     */
    <X> ZippedFlow<T, X> zipWith(Flow<X> otherFlow);

    /**
     * "Stripes" together a group of flows. The output flow contains the first value from this flow, then the first
     * value from each of the other flows, in turn, then the second value from this flow, etc. The resulting flow ends
     * when this or any of the other flows runs out of values.
     * 
     * @return combined flow
     */
    Flow<T> interleave(Flow<T>... otherFlows);
}
