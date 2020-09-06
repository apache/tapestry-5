// Copyright 2010, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * The none validator is does nothing on either the client or the server; primarily it is employed
 * as the validate parameter, to override the validation specified in the {@link org.apache.tapestry5.beaneditor.Validate}
 * annotation of a property.
 */
public class None extends AbstractValidator<Void, Object>
{
    public None()
    {
        // It is inefficient if there is not a valid matching message key even though the implementation does nothing.
        // Previous releases used "required" here but that's confusing. The "private-" prefix keeps this from being
        // sent to the client (every byte counts!).
        super(null, Object.class, "private-no-validation-for-field", null);
    }

    /**
     * Does nothing.
     */
    public void render(Field field, Void constraintValue, MessageFormatter formatter, MarkupWriter writer,
                       FormSupport formSupport)
    {
    }

    /**
     * Does nothing.
     */
    public void validate(Field field, Void constraintValue, MessageFormatter formatter, Object value)
            throws ValidationException
    {
    }
}
