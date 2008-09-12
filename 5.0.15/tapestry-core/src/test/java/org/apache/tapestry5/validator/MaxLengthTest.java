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

public class MaxLengthTest extends InternalBaseTestCase
{
    @Test
    public void short_enough() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        String value = "Now the student has become the master.";

        replay();

        MaxLength validator = new MaxLength();

        validator.validate(field, value.length(), formatter, value);

        verify();
    }

    @Test
    public void long_value() throws Exception
    {
        String label = "My Field";
        Field field = mockFieldWithLabel(label);
        MessageFormatter formatter = mockMessageFormatter();
        String value = "Now the student has become the master.";
        String message = "{message}";
        Integer constraint = value.length() - 1;

        train_format(formatter, message, constraint, label);

        replay();

        MaxLength validator = new MaxLength();

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
