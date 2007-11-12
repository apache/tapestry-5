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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;

public class DateFieldTest extends TapestryTestCase
{

    @Test
    public void convert_symbols_success() throws Exception
    {
        Messages messages = messagesFor(DateField.class);

        DateField df = new DateField();
        df.injectMessages(messages);

        SimpleDateFormat format = df.toJavaDateFormat();

        assertEquals(format.toPattern(), "MM/dd/yy");
    }

    @Test
    public void convert_symbol_non_default() throws Exception
    {
        Messages messages = messagesFor(DateField.class);

        DateField df = new DateField();
        df.injectMessages(messages);
        df.injectFormat("%d %b %Y");

        SimpleDateFormat format = df.toJavaDateFormat();

        assertEquals(format.toPattern(), "dd MMM yyyy");
    }

    @Test
    public void unknown_symbol() throws Exception
    {
        Messages messages = messagesFor(DateField.class);
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getLocation(resources, l);

        DateField df = new DateField();
        df.injectMessages(messages);
        df.injectFormat("%d %b %Z");
        df.injectResources(resources);

        replay();

        try
        {
            df.toJavaDateFormat();
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unknown or unsupported symbol '%Z' (in format '%d %b %Z').");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }
}
