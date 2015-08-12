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
package org.apache.tapestry5.integration.app1.pages;

import java.util.Iterator;
import java.util.Map;

/**
 * Demonstrates the use of Map expressions in template expansions (eg: ${{'a': 1, 'b': 2}}
 */
public class MapExpressionInExpansions
{

    public String echoMap(Map<?,?> value)
    {
        if (value == null)
            return "";

        StringBuilder builder = new StringBuilder("{");

        for (Map.Entry entry : value.entrySet())
        {
            builder.append(entry.getKey())
                   .append(':')
                   .append(entry.getValue())
                   .append(',');
        }

        if (builder.length() > 1)
            builder.deleteCharAt(builder.length()-1);

        builder.append('}');

        return builder.toString();
    }

}
