// Copyright 2010, 2011 The Apache Software Foundation
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

import java.util.Map;

/**
 * The result of the {@link Flow#zipWith(Flow)} method (or created from a Map via {@link F#zippedFlow(Map)}), a Flow of
 * combined {@link Tuple} values (that can be deconstructed, eventually, using {@link #unzip()}).
 * 
 * @param <A> first type
 * @param <B> second type
 * @since 5.3
 */
public interface ZippedFlow<A, B> extends FlowOperations<Tuple<A, B>, ZippedFlow<A, B>>
{
    /**
     * Mapping for zipped flows; a mapper is used to map tuples of this zipped flow into new tuples
     * with a new type, forming the resulting zipped flow. This is a lazy operation.
     */
    <X, Y> ZippedFlow<X, Y> mapTuples(Mapper<Tuple<A, B>, Tuple<X, Y>> mapper);

    /**
     * A ZippedFlow is a Flow of Tuples; this inverts that, splitting each Tuple into
     * a Flow of elements, then assembling the result as a Tuple of two values. This is a lazy
     * operation.
     * 
     * @return two flows of unzipped Tuples
     */
    Tuple<Flow<A>, Flow<B>> unzip();

    /**
     * Returns a flow of the first values of the tuples of the zipped flow. This is a lazy
     * operation.
     */
    Flow<A> firsts();

    /**
     * Returns a flow of the second values of the tuples of the zipped flow. This is a lazy
     * operation.
     */
    Flow<B> seconds();

    /**
     * Filters the tuples in the zipped flow by applying a predicate to the first value in each tuple.
     * This is a lazy operation.
     */
    ZippedFlow<A, B> filterOnFirst(Predicate<? super A> predicate);

    /**
     * Filters the tuples in the zipped flow by applying a predicate to the second value in each tuple. This
     * is a lazy operations.
     */
    ZippedFlow<A, B> filterOnSecond(Predicate<? super B> predicate);

    /**
     * Removes tuples from the zipped flow by applying a predicate to the first value in each tuple.
     * This is a lazy operation.
     */
    ZippedFlow<A, B> removeOnFirst(Predicate<? super A> predicate);

    /**
     * Removes tuples from the zipped flow by applying a predicate to the second value in each tuple. This
     * is a lazy operations.
     */
    ZippedFlow<A, B> removeOnSecond(Predicate<? super B> predicate);

    /**
     * Constructs a HashMap by converting the tuples of the zipped flow into keys (first tuple value) and values (second
     * tuple value).
     */
    Map<A, B> toMap();
}
