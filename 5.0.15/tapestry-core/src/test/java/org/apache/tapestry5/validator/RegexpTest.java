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

package org.apache.tapestry5.validator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

/**
 * These are getting tedious; I'd rather do it via integration tests.
 */
public class RegexpTest extends InternalBaseTestCase
{
    @Test
    public void matching_pattern() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Pattern constraint = Pattern.compile("\\d{4}");

        replay();

        Regexp validator = new Regexp();

        validator.validate(field, constraint, formatter, "1234");

        verify();
    }

    @Test
    public void input_mismatch() throws Exception
    {
        String label = "My Field";
        Field field = mockFieldWithLabel(label);
        MessageFormatter formatter = mockMessageFormatter();
        String message = "{message}";
        Pattern constraint = Pattern.compile("\\d{4}");
        String value = "abc";

        train_format(formatter, message, constraint.toString(), label);

        replay();

        Regexp validator = new Regexp();

        try
        {
            validator.validate(field, constraint, formatter, value);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), message);
        }

        verify();

    }
}
