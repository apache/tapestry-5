// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.json;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Implementations of {@link java.util.stream.Collector} that implement reductions
 * to {@code JSONArray} and to {@code JSONObject}.
 * 
 * @since 5.7
 */

public final class JSONCollectors
{
    private JSONCollectors()
    {
    }

    /**
     * Returns a {@code Collector} that accumulates the input elements into a
     * new {@code JSONArray}.
     *
     * @param <T> the type of the input elements
     * @return a {@code Collector} which collects all the input elements into a
     *         {@code JSONArray}, in encounter order
     * @since 5.7
     */
    public static <T> Collector<T, ?, JSONArray> toArray()
    {
        return Collector.of(JSONArray::new,
                            JSONArray::add,
                            JSONArray::putAll);
    }

    /**
     * Returns a {@code Collector} that accumulates elements into a
     * {@code JSONObject} whose keys and values are the result of applying the provided
     * mapping functions to the input elements.
     *
     * In case of duplicate keys an {@code IllegalStateException} is
     * thrown when the collection operation is performed.
     *
     * @param <T> the type of the input elements
     * @param keyMapper
     *            a mapping function to produce String keys
     * @param valueMapper
     *            a mapping function to produce values
     * @return a {@code Collector} which collects elements into a {@code JSONObject}
     *         whose keys and values are the result of applying mapping functions to
     *         the input elements
     * @since 5.7
     */
    public static <T> Collector<T, ?, JSONObject> toMap(Function<? super T, String> keyMapper,
                                                        Function<? super T, Object> valueMapper)
    {
        return Collectors.toMap(keyMapper,
                                valueMapper,
                                (u, v) -> {
                                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                                },
                                JSONObject::new);
    }

}
