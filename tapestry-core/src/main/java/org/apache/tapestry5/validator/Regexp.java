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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regexp extends AbstractValidator<Pattern, String>
{
    public Regexp()
    {
        super(Pattern.class, String.class, "regexp");
    }

    private String buildMessage(MessageFormatter formatter, Field field, Pattern constraintValue)
    {
        return formatter.format(constraintValue.toString(), field.getLabel());
    }

    public void render(Field field, Pattern constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        formSupport.addValidation(field, "regexp", buildMessage(formatter, field, constraintValue),
                                  constraintValue.pattern());
    }

    public void validate(Field field, Pattern constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        Matcher matcher = constraintValue.matcher(value);

        if (!matcher.matches()) throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

}
