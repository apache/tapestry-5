// Copyright 2008, 2012, 2014 The Apache Software Foundation
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
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Html5Support;
import org.testng.annotations.Test;

public class EmailTest extends InternalBaseTestCase
{
    @Test
    public void matching_pattern() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Html5Support html5Support = mockHtml5Support();
        
        replay();

        Email validator = new Email(null, html5Support);

        validator.validate(field, null, formatter, "myemail@mail.com");

        verify();
    }

    @Test
    public void input_mismatch() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Html5Support html5Support = mockHtml5Support();
        
        replay();

        Email validator = new Email(null, html5Support);

        try
        {
            validator.validate(field, null, formatter, "invalid_email");
            unreachable();
        }
        catch (ValidationException ex)
        {
        }

        try
        {
            validator.validate(field, null, formatter, "@mail.com");
            unreachable();
        }
        catch (ValidationException ex)
        {
        }

        // TAP5-2282
        try
        {
            validator.validate(field, null, formatter, "aaa@bbb/cc");
            unreachable();
        }
        catch (ValidationException ex)
        {
        }

        verify();

    }
}
