// Copyright 2006, 2007 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class RequiredTest extends TapestryTestCase
{
    @Test
    public void null_value()
    {
        Field field = mockFieldWithLabel("My Field");
        MessageFormatter formatter = mockMessageFormatter();

        train_format(formatter, "{message}", "My Field");

        replay();

        try
        {
            new Required().validate(field, null, formatter, null);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), "{message}");
        }

        verify();
    }

    @Test
    public void blank_value()
    {
        MessageFormatter formatter = mockMessageFormatter();
        Field field = mockFieldWithLabel("My Field");

        train_format(formatter, "{message}", "My Field");

        replay();

        try
        {
            new Required().validate(field, null, formatter, "");
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), "{message}");
        }

        verify();
    }

    @Test
    public void non_blank_value() throws Exception
    {
        MessageFormatter formatter = mockMessageFormatter();
        Field field = mockField();

        replay();

        new Required().validate(field, null, formatter, "not null");

        verify();
    }
}
