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

package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.Field;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.json.JSONObject;
import org.testng.annotations.Test;

public class FormSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void execute_deferred_with_no_commands()
    {
        FormSupportImpl support = new FormSupportImpl();

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

        FormSupportImpl support = new FormSupportImpl();

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

        FormSupportImpl support = new FormSupportImpl();

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
        FormSupportImpl support = new FormSupportImpl();

        String encodingType = "foo/bar";

        support.setEncodingType(encodingType);

        assertSame(support.getEncodingType(), encodingType);
    }

    @Test
    public void set_encoding_type_to_same_value_is_allowed()
    {
        FormSupportImpl support = new FormSupportImpl();

        String encodingType = "foo/bar";

        support.setEncodingType(encodingType);
        support.setEncodingType(new String(encodingType));

        assertEquals(support.getEncodingType(), encodingType);
    }

    @Test
    public void set_encoding_type_conflict()
    {

        FormSupportImpl support = new FormSupportImpl();

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
        Field barney = newField("barney");
        Field fred = newField("fred");

        replay();

        FormSupportImpl support = new FormSupportImpl();

        support.addValidation(barney, "required", "Who can live without Barney?", null);
        support.addValidation(barney, "email", "You know, an e-mail address.", null);
        support.addValidation(fred, "maxlength", "Up to 10 characters", 10);

        verify();


        JSONObject validations = support.getValidations();

        // Tip-toe around the fact that the order of the keys is JDK specific.

        assertEquals(CollectionFactory.newSet(validations.keys()), CollectionFactory.newSet("fred", "barney"));

        assertEquals(validations.get("fred").toString(), "[[\"maxlength\",\"Up to 10 characters\",10]]");
        assertEquals(validations.get("barney").toString(),
                     "[[\"required\",\"Who can live without Barney?\"],[\"email\",\"You know, an e-mail address.\"]]");


    }

    private Field newField(String clientId)
    {
        Field field = newMock(Field.class);

        expect(field.getClientId()).andReturn(clientId).atLeastOnce();

        return field;
    }


}
