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

/**
 * The result of the {@link Flow#zipWith(Flow)} method, a Flow of combined {@link Tuple} values
 * (that can be deconstructed, eventually, using {@link #unzip()}). Each operation of {@link Flow}
 * has a corresponding implementation here, on the Tuple values.
 * 
 * @param <A>
 * @param <B>
 * @since 5.3.0
 */
public interface ZippedFlow<A, B> extends FlowOperations<Tuple<A, B>, ZippedFlow<A, B>>
{
    /**
     * Mapping for zipped flows; a mapper is used to map tuples of this zipped flow into new tuples
     * with a new type, forming the resulting zipped flow.
     */
    <X, Y> ZippedFlow<X, Y> mapTuples(Mapper<Tuple<A, B>, Tuple<X, Y>> mapper);

    /**
     * A ZippedFlow is a Flow of Tuples; this inverts that, splitting each Tuple into
     * a Flow of values, then assembling the result as a Tuple of two values.
     * 
     * @return two flows of unzipped Tuples
     */
    Tuple<Flow<A>, Flow<B>> unzip();

    /**
     * Returns a flow of the first values of the tuples of the zipped flow.
     */
    Flow<A> firsts();

    /**
     * Returns a flow of the second values of the tuples of the zipped flow.
     */
    Flow<B> seconds();
}
