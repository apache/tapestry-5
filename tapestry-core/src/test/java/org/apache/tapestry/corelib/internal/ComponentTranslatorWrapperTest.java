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

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.runtime.ComponentEventException;
import org.apache.tapestry.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

public class ComponentTranslatorWrapperTest extends TapestryTestCase
{
    @SuppressWarnings({"unchecked"})
    @Test
    public void parse_client_via_event() throws ValidationException
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();

        String clientValue = "abracadabra";

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                Object[] context = (Object[]) args[1];
                ComponentEventHandler handler = (ComponentEventHandler) args[2];

                // Pretend that the parser event handler converted it to upper case.

                return handler.handleResult(context[0].toString().toUpperCase(), null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventHandler.class))).andAnswer(answer);


        replay();

        Object actual = new ComponentTranslatorWrapper(resources, translator).parseClient(clientValue, messages);

        assertEquals(actual, clientValue.toUpperCase());

        verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void parse_client_event_handler_throws_validation_exception() throws Exception
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationException ve = new ValidationException("Just didn't feel right.");

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventHandler.class))).andThrow(
                new ComponentEventException(ve.getMessage(), null, ve));


        replay();

        try
        {
            new ComponentTranslatorWrapper(resources, translator).parseClient(clientValue, messages);

            unreachable();
        }
        catch (ValidationException ex)
        {
            assertSame(ex, ve);
        }


        verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void parse_client_event_handler_fails_with_other_exception() throws Exception
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        RuntimeException re = new RuntimeException("Just didn't feel right.");
        ComponentEventException cee = new ComponentEventException(re.getMessage(), null, re);

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventHandler.class))).andThrow(cee);


        replay();

        try
        {
            new ComponentTranslatorWrapper(resources, translator).parseClient(clientValue, messages);

            unreachable();
        }
        catch (ComponentEventException ex)
        {
            assertSame(ex, cee);
            assertSame(ex.getCause(), re);
        }


        verify();
    }

    @Test
    public void parse_client_via_translator() throws ValidationException
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventHandler.class))).andReturn(false);

        expect(translator.parseClient(clientValue, messages)).andReturn("foobar");

        replay();

        Object actual = new ComponentTranslatorWrapper(resources, translator).parseClient(clientValue, messages);

        assertEquals(actual, "foobar");

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void to_client_via_translator()
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();

        String clientValue = "abracadabra";

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventHandler.class))).andReturn(false);

        expect(translator.toClient(value)).andReturn(clientValue);

        replay();

        Translator t = new ComponentTranslatorWrapper(resources, translator);

        assertEquals(t.toClient(value), clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void to_client_via_event_handler() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();

        final String clientValue = "abracadabra";

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventHandler handler = (ComponentEventHandler) args[2];

                return handler.handleResult(clientValue, null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventHandler.class))).andAnswer(answer);


        replay();

        Translator t = new ComponentTranslatorWrapper(resources, translator);

        assertEquals(t.toClient(value), clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    public void to_client_via_event_handler_returns_non_string() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();


        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventHandler handler = (ComponentEventHandler) args[2];

                // Return an innappropriate value.

                return handler.handleResult(this, null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(ComponentTranslatorWrapper.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventHandler.class))).andAnswer(answer);


        replay();

        Translator t = new ComponentTranslatorWrapper(resources, translator);

        try
        {
            t.toClient(value);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), InternalMessages.toClientShouldReturnString());
        }

        verify();
    }

}
