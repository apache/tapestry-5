// Copyright 2008, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.regex.Pattern;

/**
 * A validator that checks if a given string is well-formed email address. This validator is not configurable.
 * <p/>
 * Starting with release 5.4, this validator also performs client-side validation.
 */
public class Email extends AbstractValidator<Void, String>
{
    private static final Pattern PATTERN = Pattern
            .compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);

    public Email(JavaScriptSupport javaScriptSupport)
    {
        super(null, String.class, "invalid-email", javaScriptSupport);
    }

    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        if (formSupport.isClientValidationEnabled())
        {
            javaScriptSupport.require("core/validation");

            writer.getElement().attributes("data-validation", "true",
                    "data-validate-regexp", PATTERN.pattern(),
                    "data-regexp-message", formatter.toString());
        }
    }

    public void validate(Field field, Void constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        if (!PATTERN.matcher(value).matches()) throw new ValidationException(formatter.toString());
    }
}
