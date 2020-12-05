// Copyright 2011-2013 The Apache Software Foundation
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

package org.apache.tapestry5.json.modules;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.json.StringToJSONArray;
import org.apache.tapestry5.internal.json.StringToJSONObject;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

/**
 * A module that integrates JSON into Tapestry in terms of type coercions.  tapestry-json can still
 * be used independently of the rest of Tapestry (since its a 'provided' dependency),
 * but when used with tapestry-ioc on the classpath, the coercions described by this module become available.
 *
 * @since 5.3
 */
public class JSONModule
{
    /**
     * <ul>
     * <li>{@link String} to {@link org.apache.tapestry5.json.JSONObject}</li>
     * <li>{@link String} to {@link org.apache.tapestry5.json.JSONArray}</li>
     * </ul>
     * @param configuration the configuration to provide the type coercer to
     */
    @Contribute(TypeCoercer.class)
    public static void provideCoercions(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration)
    {
        CoercionTuple<String, JSONObject> stringToJsonObject = CoercionTuple.create(String.class, JSONObject.class, new StringToJSONObject());
        configuration.add(stringToJsonObject.getKey(), stringToJsonObject);

        CoercionTuple<String, JSONArray> stringToJsonArray = CoercionTuple.create(String.class, JSONArray.class, new StringToJSONArray());
        configuration.add(stringToJsonArray.getKey(), stringToJsonArray);
    }
}
