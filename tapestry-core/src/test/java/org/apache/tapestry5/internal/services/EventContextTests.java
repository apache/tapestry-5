// Copyright 2010, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EventContextTests extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }

    @Test
    public void array_event_context_to_strings()
    {
        EventContext ec = new ArrayEventContext(typeCoercer, 1, 2.3);

        assertEquals(ec.toStrings(), new String[]
        { "1", "2.3" });
    }

    @Test
    public void empty_event_context_to_strings()
    {
        assertEquals(new EmptyEventContext().toStrings(), new String[0]);
    }

    @Test
    public void to_string_of_event_context() {

        EventContext ec = new ArrayEventContext(typeCoercer, 1, 2.3);

        assertEquals(ec.toString(), "<EventContext: 1, 2.3>");
    }
}
