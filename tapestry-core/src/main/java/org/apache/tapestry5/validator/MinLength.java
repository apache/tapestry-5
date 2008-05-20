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

package org.apache.tapestry5.validator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.services.FormSupport;

/**
 * Validates that a string value has a minimum length.
 */
public final class MinLength extends AbstractValidator<Integer, String>
{
    public MinLength()
    {
        super(Integer.class, String.class, "minimum-string-length");
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

    public void render(Field field, Integer constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        formSupport.addValidation(field, "minlength", buildMessage(formatter, field, constraintValue), constraintValue);
    }
}
