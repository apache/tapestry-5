// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.testng.annotations.Test;

public class FormSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void execute_deferred_with_no_commands()
    {
        FormSupportImpl support = new FormSupportImpl(null, null);

        support.executeDeferred();
    }

    @Test
    public void execute_deferred_execute_in_added_order()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        getMocksControl().checkOrder(true);

        r1.run();
        r2.run();

        replay();

        FormSupportImpl support = new FormSupportImpl(null, null);

        support.defer(r1);
        support.defer(r2);

        support.executeDeferred();

        verify();
    }

    @Test
    public void deferred_commands_execute_once()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();
        Runnable r3 = mockRunnable();

        getMocksControl().checkOrder(true);

        r1.run();
        r2.run();

        replay();

        FormSupportImpl support = new FormSupportImpl(null, null);

        support.defer(r1);
        support.defer(r2);

        support.executeDeferred();

        verify();

        r3.run();

        replay();

        support.defer(r3);

        support.executeDeferred();

        verify();
    }

    @Test
    public void set_encoding_type()
    {
        FormSupportImpl support = new FormSupportImpl(null, null);

        String encodingType = "foo/bar";

        support.setEncodingType(encodingType);

        assertSame(support.getEncodingType(), encodingType);
    }

    @Test
    public void set_encoding_type_to_same_value_is_allowed()
    {
        FormSupportImpl support = new FormSupportImpl(null, null);

        String encodingType = "foo/bar";

        support.setEncodingType(encodingType);
        support.setEncodingType(new String(encodingType));

        assertEquals(support.getEncodingType(), encodingType);
    }

    @Test
    public void set_encoding_type_conflict()
    {

        FormSupportImpl support = new FormSupportImpl(null, null);

        support.setEncodingType("foo");
        try
        {
            support.setEncodingType("bar");
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(),
                         "Encoding type of form has already been set to \'foo\' and may not be changed to \'bar\'.");
        }
    }

    @Test
    public void add_validations()
    {
        Field barney = mockField();
        ClientBehaviorSupport clientBehaviorSupport = mockClientBehaviorSupport();

        clientBehaviorSupport.addValidation(barney, "required", "Who can live without Barney?", null);

        replay();

        FormSupportImpl support = new FormSupportImpl(null, null, null, clientBehaviorSupport, true, null,
                                                      null);

        support.addValidation(barney, "required", "Who can live without Barney?", null);

        verify();
    }

    @Test
    public void add_validation_when_client_validation_is_disabled()
    {
        Field barney = mockField();
        ClientBehaviorSupport clientBehaviorSupport = mockClientBehaviorSupport();

        replay();

        FormSupportImpl support = new FormSupportImpl(null, null, null, clientBehaviorSupport, false, null,
                                                      null);

        support.addValidation(barney, "required", "Who can live without Barney?", null);

        verify();
    }
}
