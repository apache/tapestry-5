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

package org.apache.tapestry.validator;

import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.Validator;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.services.FormSupport;

/**
 * A validator that enforces that the value is not null and not the empty string. This validator is
 * not configurable.
 */
public final class Required implements Validator<Void, Object>
{
    public String getMessageKey()
    {
        return "required";
    }

    public void validate(Field field, Void constraintValue, MessageFormatter formatter, Object value)
            throws ValidationException
    {
        if (value == null || value.toString().equals("")) throw new ValidationException(buildMessage(formatter, field));
    }

    private String buildMessage(MessageFormatter formatter, Field field)
    {
        return formatter.format(field.getLabel());
    }

    public Class<Void> getConstraintType()
    {
        return null;
    }

    public boolean invokeIfBlank()
    {
        return true;
    }

    public Class<Object> getValueType()
    {
        return Object.class;
    }

    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        formSupport.addValidation(field, "required", buildMessage(formatter, field), null);
    }
}
