// Copyright 2007-2013 The Apache Software Foundation
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
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.DataConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Validates that a string value has not exceeded a maximum length.
 */
public final class MaxLength extends AbstractValidator<Integer, String>
{
    public MaxLength(JavaScriptSupport javaScriptSupport)
    {
        super(Integer.class, String.class, "maximum-string-length", javaScriptSupport);
    }

    public void validate(Field field, Integer constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        if (value.length() > constraintValue)
            throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

    private String buildMessage(MessageFormatter formatter, Field field, Integer constraintValue)
    {
        return formatter.format(constraintValue, field.getLabel());
    }

    public void render(Field field, Integer constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        if (formSupport.isClientValidationEnabled())
        {
            if (javaScriptSupport.isRequireJsEnabled())
            {
                javaScriptSupport.require("t5/core/validation");
            }
            else
            {
                javaScriptSupport.importEsModule("t5/core/validation");
            }

            writer.attributes(DataConstants.VALIDATION_ATTRIBUTE, true,
                    "data-validate-max-length", constraintValue.toString(),
                    "data-max-length-message", buildMessage(formatter, field, constraintValue));
        }
    }
}
