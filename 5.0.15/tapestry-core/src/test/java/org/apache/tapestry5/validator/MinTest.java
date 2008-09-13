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

public class MinTest extends InternalBaseTestCase
{
    @Test
    public void large_enough() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Long constraint = 50L;

        replay();

        Min validator = new Min();

        for (int value = 50; value < 52; value++)
            validator.validate(field, constraint, formatter, value);

        verify();
    }

    @Test
    public void value_too_small() throws Exception
    {
        String label = "My Field";
        Field field = mockFieldWithLabel(label);
        MessageFormatter formatter = mockMessageFormatter();
        String message = "{message}";
        Long constraint = 100l;
        Number value = 99;

        train_format(formatter, message, constraint, label);

        replay();

        Min validator = new Min();

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
