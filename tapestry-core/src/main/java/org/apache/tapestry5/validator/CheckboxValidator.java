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
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.DataConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A validator that enforces that the value is true or false. This validator is not configurable.
 *
 * @since 5.5.0
 */
class CheckboxValidator extends AbstractValidator<Void, Object>
{
    private final Boolean expectedValue;
    private final String expectedStatus;

    public CheckboxValidator(JavaScriptSupport javaScriptSupport, String messageKey, Boolean expectedValue, String expectedStatus)
    {
        super(null, Object.class, messageKey, javaScriptSupport);
        this.expectedValue = expectedValue;
        this.expectedStatus = expectedStatus;
    }

    @Override
    public void validate(Field field, Void constraintValue, MessageFormatter formatter, Object value)
            throws ValidationException
    {
        if (!expectedValue.equals(value))
            throw new ValidationException(buildMessage(formatter, field));
    }

    private String buildMessage(MessageFormatter formatter, Field field)
    {
        return formatter.format(field.getLabel());
    }

    /**
     * The exception to the rule.
     */
    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        if (formSupport.isClientValidationEnabled())
        {
            javaScriptSupport.require("t5/core/validation");

            writer.attributes(
                    DataConstants.VALIDATION_ATTRIBUTE, true,
                    "data-expected-status", expectedStatus,
                    "data-checked-message", buildMessage(formatter, field));
        }
    }

    @Override
    public String toString()
    {
        return "Checkbox must be " + expectedStatus + " validator";
    }
}
