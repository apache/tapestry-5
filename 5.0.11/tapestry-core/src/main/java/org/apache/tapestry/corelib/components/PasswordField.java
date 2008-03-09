// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.corelib.base.AbstractTextField;

/**
 * A version of {@link TextField}, but rendered out as an &lt;input type="password"&gt; element. Further, the output
 * value for a PasswordField is always blank.
 * <p/>
 * Includes the <code>size</code> attribute, if a {@link org.apache.tapestry.beaneditor.Width} annotation is present on
 * the property bound to the value parameter.
 */
public class PasswordField extends AbstractTextField
{

    @Override
    protected final void writeFieldTag(MarkupWriter writer, String value)
    {
        writer.element("input",

                       "type", "password",

                       "name", getControlName(),

                       "id", getClientId(),

                       "value", "",

                       "size", getWidth());
    }

    final void afterRender(MarkupWriter writer)
    {
        writer.end(); // input
    }

}
