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

package org.apache.tapestry.validator;

import org.apache.tapestry.Field;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.MessageFormatter;
import org.testng.annotations.Test;

public class MaxTest extends InternalBaseTestCase
{
    @Test
    public void small_enough() throws Exception
    {
        Field field = newField();
        MessageFormatter formatter = newMessageFormatter();
        Long constraint = 50l;

        replay();

        Max validator = new Max();

        for (int value = 48; value <= 50; value++)
            validator.validate(field, constraint, formatter, value);

        verify();
    }

    @Test
    public void value_too_large() throws Exception
    {
        String label = "My Field";
        Field field = newFieldWithLabel(label);
        MessageFormatter formatter = newMessageFormatter();
        String message = "{message}";
        Long constraint = 100l;
        Number value = 101;

        train_format(formatter, message, constraint, label);

        replay();

        Max validator = new Max();

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
