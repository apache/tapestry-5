// Copyright 2018 The Apache Software Foundation
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

package org.apache.tapestry5.validator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Html5Support;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A validator that enforces that the value is true. This validator is not configurable.
 * 
 * @since 5.5.0
 */
public final class Checked extends CheckboxValidator
{
    final private Html5Support html5Support;
    
    public Checked(JavaScriptSupport javaScriptSupport, Html5Support html5Support)
    {
        super(javaScriptSupport, "checked", Boolean.TRUE, "checked");
        this.html5Support = html5Support;
    }

    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        super.render(field, constraintValue, formatter, writer, formSupport);

        if (html5Support.isHtml5SupportEnabled())
        {
            writer.attributes("required", "required");
        }
    }
}
