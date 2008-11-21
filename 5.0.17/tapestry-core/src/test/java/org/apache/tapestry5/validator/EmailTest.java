// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.testng.annotations.Test;

public class EmailTest extends InternalBaseTestCase
{
    @Test
    public void matching_pattern() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();

        replay();

        Email validator = new Email();

        validator.validate(field, null, formatter, "myemail@mail.com");

        verify();
    }

    @Test
    public void input_mismatch() throws Exception
    {
        String label = "My Field";
        Field field = mockFieldWithLabel(label);
        MessageFormatter formatter = mockMessageFormatter();
        String message = "{message}";

        train_format(formatter, message, label);

        replay();

        Email validator = new Email();

        try
        {
            validator.validate(field, null, formatter, "invalid_email");
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), message);

            verify();
        }

        field = mockFieldWithLabel(label);

        train_format(formatter, message, label);

        replay();

        try
        {
            validator.validate(field, null, formatter, "@mail.com");
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), message);

            verify();
        }

    }
}
