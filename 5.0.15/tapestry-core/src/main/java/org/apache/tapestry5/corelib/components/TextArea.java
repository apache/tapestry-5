// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.corelib.base.AbstractTextField;

/**
 * TextArea component corresponds to a &lt;textarea&gt; element. The value parameter is almost always bound to a string,
 * but this is not an absolute requirement.
 * <p/>
 * Includes the <code>cols</code> attribute, if a {@link org.apache.tapestry5.beaneditor.Width} annotation is present on
 * the property bound to the value parameter.
 *
 * @see org.apache.tapestry5.corelib.components.TextOutput
 */
public class TextArea extends AbstractTextField
{
    private String value;

    @Override
    protected final void writeFieldTag(MarkupWriter writer, String value)
    {
        writer.element("textarea",

                       "name", getControlName(),

                       "id", getClientId(),

                       "cols", getWidth());

        // Save until needed in after()

        this.value = value;
    }

    final void afterRender(MarkupWriter writer)
    {
        // TextArea will not have a template.

        if (value != null) writer.write(value);

        writer.end(); // textarea
    }

}
