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

/* A vaidator that enforces that a number is greater than some minimum integer value. */
public class Min extends AbstractValidator<Long, Number>
{
    public Min()
    {
        super(Long.class, Number.class, "min-integer");
    }

    public void validate(Field field, Long constraintValue, MessageFormatter formatter, Number value)
            throws ValidationException
    {
        if (value.longValue() < constraintValue)
            throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

    private String buildMessage(MessageFormatter formatter, Field field, Long constraintValue)
    {
        return formatter.format(constraintValue, field.getLabel());
    }

    public void render(Field field, Long constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        formSupport.addValidation(field, "min", buildMessage(formatter, field, constraintValue), constraintValue);
    }
}
