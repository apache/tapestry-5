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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enforces that the input matches a provided regular expression.
 *
 * Starting in 5.4, this always writes the pattern and title attribute, even when client validation is not enabled.
 * The title attribute is used specially by modern browsers, in concert with pattern.
 */
public class Regexp extends AbstractValidator<Pattern, String>
{
    public Regexp(JavaScriptSupport javaScriptSupport)
    {
        super(Pattern.class, String.class, "regexp", javaScriptSupport);
    }

    private String buildMessage(MessageFormatter formatter, Field field, Pattern constraintValue)
    {
        return formatter.format(constraintValue.toString(), field.getLabel());
    }

    public void render(Field field, Pattern constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        String message = buildMessage(formatter, field, constraintValue);

        if (formSupport.isClientValidationEnabled())
        {
            javaScriptSupport.require("t5/core/validation");

            writer.attributes(DataConstants.VALIDATION_ATTRIBUTE, true,
                    "data-validate-regexp", constraintValue.pattern(),
                    "data-regexp-message", message,
                    "pattern", constraintValue.pattern(),
                    "title", message);
        }
    }

    public void validate(Field field, Pattern constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        Matcher matcher = constraintValue.matcher(value);

        if (!matcher.matches()) throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

}
