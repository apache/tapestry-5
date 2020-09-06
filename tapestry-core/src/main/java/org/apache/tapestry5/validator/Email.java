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
import org.apache.tapestry5.services.Html5Support;
import org.apache.tapestry5.services.javascript.DataConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.regex.Pattern;

/**
 * A validator that checks if a given string is well-formed email address. This validator is not configurable.
 *
 * Starting with release 5.4, this validator also performs client-side validation.
 */
public class Email extends AbstractValidator<Void, String>
{

    // The client-side uses a similar RE, but converts the input to lower case before applying the pattern.
    // See validation.coffee.

    private static final Pattern PATTERN = Pattern
            .compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);
    
    final private Html5Support html5Support;

    public Email(JavaScriptSupport javaScriptSupport, Html5Support html5Support)
    {
        super(null, String.class, "invalid-email", javaScriptSupport);
        this.html5Support = html5Support;
    }

    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
        if (formSupport.isClientValidationEnabled())
        {
            javaScriptSupport.require("t5/core/validation");

            writer.attributes(
                    DataConstants.VALIDATION_ATTRIBUTE, true,
                    "data-validate-email", true,
                    "data-email-message", formatter.toString());
        }
        
        if (html5Support.isHtml5SupportEnabled()) {
            writer.getElement().forceAttributes("type", "email");
        }
        
    }

    public void validate(Field field, Void constraintValue, MessageFormatter formatter, String value)
            throws ValidationException
    {
        if (!PATTERN.matcher(value).matches()) throw new ValidationException(formatter.toString());
    }
}
