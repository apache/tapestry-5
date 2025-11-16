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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException;
import org.junit.jupiter.api.Test;

public class JSONTest {

    @Test
    void invalidTypesThrowJSONInvalidTypeException() {
        Map<String, String> invalidValue = Collections.emptyMap();

        JSONInvalidTypeException e = assertThrows(JSONInvalidTypeException.class, () -> {
            JSON.testValidity(invalidValue);
        });

        String expectedMessage = String.format(
                "JSONArray values / JSONObject properties may be one of Boolean, Number, String, %s, %s, %s, %s, %s. Type %s is not allowed.",
                "org.apache.tapestry5.json.JSONArray",
                "org.apache.tapestry5.json.JSONLiteral",
                "org.apache.tapestry5.json.JSONObject",
                "org.apache.tapestry5.json.JSONObject$Null",
                "org.apache.tapestry5.json.JSONString",
                invalidValue.getClass().getName());
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void nullIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            JSON.testValidity(null);
        });
    }
}