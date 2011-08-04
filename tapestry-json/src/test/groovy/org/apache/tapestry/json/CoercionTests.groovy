// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry.json

import org.testng.annotations.Test
import org.apache.tapestry5.internal.json.StringToJSONObject
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.internal.json.StringToJSONArray

/**
 *
 */
class CoercionTests {

    @Test
    void string_to_JSONObject()
    {
        def expected = new JSONObject().put("foo", "bar")

        assert new StringToJSONObject().coerce("{ 'foo' : 'bar' }") == expected
    }

    @Test
    void string_to_JSONArray() {
        def expected = new JSONArray(1, 2, "three");

        assert new StringToJSONArray().coerce("[ 1, 2, 'three' ]") == expected

    }
}
