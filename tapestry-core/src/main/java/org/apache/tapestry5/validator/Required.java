// Copyright 2006, 2007, 2008, 2012, 2014 The Apache Software Foundation
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
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Html5Support;
import org.apache.tapestry5.services.javascript.DataConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A validator that enforces that the value is not null and not the empty string. This validator is not configurable.
 */
public final class Required extends AbstractValidator<Void, Object>
{
    final private Html5Support html5Support;
    
    public Required(JavaScriptSupport javaScriptSupport, Html5Support html5Support)
    {
        super(null, Object.class, "required", javaScriptSupport);
        this.html5Support = html5Support;
    }

    public void validate(Field field, Void constraintValue, MessageFormatter formatter, Object value)
            throws ValidationException
    {
        if (value == null || InternalUtils.isEmptyCollection(value) || InternalUtils.isBlank(value.toString()))
            throw new ValidationException(buildMessage(formatter, field));
    }

    private String buildMessage(MessageFormatter formatter, Field field)
    {
        return formatter.format(field.getLabel());
    }

    /**
     * The exception to the rule.
     */
    public boolean isRequired()
    {
        return true;
    }

    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {

        writer.attributes("aria-required", "true");
        
        if (formSupport.isClientValidationEnabled())
        {
            javaScriptSupport.require("t5/core/validation");

            writer.attributes(
                    DataConstants.VALIDATION_ATTRIBUTE, true,
                    "data-optionality", "required",
                    "data-required-message", buildMessage(formatter, field),
                    "aria-required", "true");
        }
        if (html5Support.isHtml5SupportEnabled())
        {
            writer.attributes("required", "required");
        }
    }
}
