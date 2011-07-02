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

package org.apache.tapestry5.root

import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.ioc.services.TypeCoercer
import org.apache.tapestry5.json.JSONObject
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.apache.tapestry5.json.JSONArray

/**
 * @since 5.3
 */
class StringToJSONTests extends InternalBaseTestCase {

  TypeCoercer typeCoercer

  @BeforeClass
  void setup() {
    typeCoercer = getService(TypeCoercer.class)
  }


  @Test
  void string_to_JSONObject() {
    assert typeCoercer.coerce(/{ "fred" : "flinstone" }/, JSONObject.class) == new JSONObject("fred", "flinstone")
  }

  @Test
  void string_to_JSONArray() {
    assert typeCoercer.coerce(/[1, 2, 3]/, JSONArray.class) == new JSONArray(1, 2, 3)
  }

}
