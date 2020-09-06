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

package org.apache.tapestry5.internal.renderers;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.services.ObjectRenderer;

/**
 * Renders out a {@link AvailableValues} instance as a &lt;div&gt; enclosing a &lt;ul&gt;.
 * 
 * @since 5.2.0
 */
public class AvailableValuesRenderer implements ObjectRenderer<AvailableValues>
{
    public void render(AvailableValues values, MarkupWriter writer)
    {
        writer.element("div", "class", "t-available-values");

        writer.element("p");

        writer.writef("%s:", values.getValueType());

        writer.end();

        writer.element("ul");

        for (String value : values.getValues())
        {
            writer.element("li");
            writer.write(value);
            writer.end();
        }

        writer.end();

        writer.end(); // div
    }

}
