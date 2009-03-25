// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.json;

/**
 * A way of including some text (often, text that violates the normal JSON specification) as part of a JSON object or
 * array. This is used in a few places where data is nominally JSON but actually includes some non-conformant elements,
 * such as an inline function definition.
 *
 * @since 5.1.0.2
 */
public class JSONLiteral
{
    private final String text;

    public JSONLiteral(String text)
    {
        this.text = text;
    }

    /**
     * Returns the text property; this is also the value placed into the JSON string (unquoted, exactly as is).
     *
     * @return the text
     */
    @Override
    public String toString()
    {
        return text;
    }
}

