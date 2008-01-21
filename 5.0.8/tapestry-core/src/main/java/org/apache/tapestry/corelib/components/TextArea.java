// Copyright 2006, 2007 The Apache Software Foundation
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
 * TextArea component corresponds to a &lt;textarea&gt; element. The value parameter is almost
 * always bound to a string, but this is not an absolute requirement.
 *
 * @see TextOutput
 */
public final class TextArea extends AbstractTextField
{
    private String _value;

    @Override
    protected final void writeFieldTag(MarkupWriter writer, String value)
    {
        writer.element("textarea",

                       "name", getElementName(),

                       "id", getClientId());

        // Save until needed in after()

        _value = value;
    }

    final void afterRender(MarkupWriter writer)
    {
        // TextArea will not have a template.

        if (_value != null) writer.write(_value);

        writer.end(); // textarea
    }

}
