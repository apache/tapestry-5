// Copyright 2025 The Apache Software Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.tapestry5.func.Tuple;
import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException;
import org.junit.jupiter.api.Test;

public class JSONCollectorsTest {

    @Test
    void collectStreamToArray() {

        String stringValue = "a string value";
        long longValue = 3L;
        Stream<Object> stream = Stream.of(stringValue, longValue);

        JSONArray collected = stream.collect(JSONCollectors.toArray());

        assertNotNull(collected);
        assertEquals(2, collected.size());
        assertEquals(stringValue, collected.get(0));
        assertEquals(longValue, collected.get(1));
    }

    @Test
    void collectStreamToArrayInvalidType() {

        String stringValue = "a string value";
        long longValue = 3L;
        Object invalidValue = new java.util.Date();

        Stream<Object> stream = Stream.of(stringValue, longValue, invalidValue);

        assertThrows(JSONInvalidTypeException.class, () -> {
            stream.collect(JSONCollectors.toArray());
        });
    }

    @Test
    void collectStreamToMap() {

        Tuple<String, Object> first = new Tuple<>("first key", "a string value");
        Tuple<String, Object> second = new Tuple<>("second key", 3L);

        Stream<Tuple<String, Object>> stream = Stream.of(first, second);

        JSONObject collected = stream
                .collect(JSONCollectors.toMap(t -> t.first, t -> t.second));

        assertNotNull(collected);
        assertEquals(2, collected.size());
        assertEquals(first.second, collected.get(first.first));
        assertEquals(second.second, collected.get(second.first));
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" }) // Suppress warnings for Tuple generic types
    void collectStreamToMapInvalidType() {

        Tuple<String, Object> first = new Tuple<>("first key", "a string value");
        Tuple<String, Object> second = new Tuple<>("second key", 3L);
        Tuple<String, Object> third = new Tuple<>("invalid type", new java.util.Date());

        Stream<Tuple<String, Object>> stream = Stream.of(first, second, third);

        assertThrows(JSONInvalidTypeException.class, () -> {
            stream.collect(JSONCollectors.toMap(t -> t.first, t -> t.second));
        });
    }

    @Test
    void collectStreamToMapDuplicateKey() {

        assertThrows(IllegalStateException.class, () -> {
            Stream.of("first", "second", "first").collect(JSONCollectors.toMap(v -> v, v -> v));
        });
    }
}