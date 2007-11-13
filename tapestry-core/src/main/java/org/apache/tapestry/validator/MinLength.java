// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.validator;

import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.Validator;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.services.FormSupport;

/**
 * Validates that a string value has a minimum length.
 */
public final class MinLength implements Validator<Integer, String>
{
    public String getMessageKey()
    {
        return "minimum-string-length";
    }

    public void validate(Field field, Integer constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        if (value.length() < constraintValue)
            throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

    private String buildMessage(MessageFormatter formatter, Field field, Integer constraintValue)
    {
        return formatter.format(constraintValue, field.getLabel());
    }

    public Class<Integer> getConstraintType()
    {
        return Integer.class;
    }

    public boolean invokeIfBlank()
    {
        return false;
    }

    public Class<String> getValueType()
    {
        return String.class;
    }

    public void render(Field field, Integer constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        formSupport.addValidation(field, "minlength", buildMessage(formatter, field, constraintValue), constraintValue);
    }
}
