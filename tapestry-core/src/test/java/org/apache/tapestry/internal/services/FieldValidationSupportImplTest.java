// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.*;
import org.apache.tapestry.corelib.internal.InternalMessages;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.ValidationMessagesSource;
import org.apache.tapestry.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

import java.util.Locale;

public class FieldValidationSupportImplTest extends TapestryTestCase
{

    @SuppressWarnings({"unchecked"})
    @Test
    public void parse_client_via_event() throws ValidationException
    {
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        String clientValue = "abracadabra";

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                Object[] context = (Object[]) args[1];
                ComponentEventCallback handler = (ComponentEventCallback) args[2];

                // Pretend that the parser event handler converted it to upper case.

                return handler.handleResult(context[0].toString().toUpperCase(), null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();


        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        Object actual = support.parseClient(clientValue, resources, translator);

        assertEquals(actual, clientValue.toUpperCase());

        verify();
    }

    @Test
    public void to_client_for_null_value_returns_null_and_bypasses_events_and_translator() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        replay();


        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        assertNull(support.parseClient(null, resources, translator));


        verify();
    }

    @Test
    public void parse_client_for_blank_string_returns_null_and_bypasses_events_and_translator() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationException ve = new ValidationException("Just didn't feel right.");
        ValidationMessagesSource source = mockValidationMessagesSource();


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        assertNull(support.parseClient("", resources, translator));

        verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void parse_client_event_handler_throws_validation_exception() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationException ve = new ValidationException("Just didn't feel right.");
        ValidationMessagesSource source = mockValidationMessagesSource();

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andThrow(
                new RuntimeException(ve));


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        try
        {
            support.parseClient(clientValue, resources, translator);

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
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        RuntimeException re = new RuntimeException("Just didn't feel right.");
        ValidationMessagesSource source = mockValidationMessagesSource();

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andThrow(re);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        try
        {
            support.parseClient(clientValue, resources, translator);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertSame(ex, re);
        }


        verify();
    }

    @Test
    public void parse_client_via_translator() throws ValidationException
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();
        Locale locale = Locale.GERMAN;

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.PARSE_CLIENT_EVENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andReturn(false);

        train_getLocale(resources, locale);

        train_getValidationMessages(source, locale, messages);

        expect(translator.parseClient(clientValue, messages)).andReturn("foobar");

        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        Object actual = support.parseClient(clientValue, resources, translator);

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
        ValidationMessagesSource source = mockValidationMessagesSource();

        String clientValue = "abracadabra";

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andReturn(false);

        expect(translator.toClient(value)).andReturn(clientValue);

        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        String actual = support.toClient(value, resources, translator);

        assertEquals(actual, clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void to_client_via_event_handler() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        final String clientValue = "abracadabra";

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventCallback handler = (ComponentEventCallback) args[2];

                return handler.handleResult(clientValue, null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        String actual = support.toClient(value, resources, translator);

        assertEquals(actual, clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    public void to_client_via_event_handler_returns_non_string() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        Translator translator = mockTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventCallback handler = (ComponentEventCallback) args[2];

                // Return an innappropriate value.

                return handler.handleResult(this, null, null);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.TO_CLIENT_EVENT),
                                               EasyMock.aryEq(new Object[]{value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        try
        {

            support.toClient(value, resources, translator);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), InternalMessages.toClientShouldReturnString());
        }

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void event_triggered_after_delegate_invoked() throws Exception
    {
        getMocksControl().checkOrder(true);

        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        Object value = new Object();

        fv.validate(value);

        ComponentEventCallback handler = null;

        expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.VALIDATE_EVENT),
                                      EasyMock.aryEq(new Object[]{value}), EasyMock.eq(handler))).andReturn(true);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);

        support.validate(value, resources, fv);

        verify();
    }

    @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
    @Test
    public void event_trigger_throws_validation_exception() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();
        ValidationMessagesSource source = mockValidationMessagesSource();

        Object value = new Object();

        ValidationException ve = new ValidationException("Bah!");
        RuntimeException re = new RuntimeException(ve);

        ComponentEventCallback handler = null;

        fv.validate(value);

        expect(resources.triggerEvent(EasyMock.eq(FieldValidationSupportImpl.VALIDATE_EVENT),
                                      EasyMock.aryEq(new Object[]{value}), EasyMock.eq(handler))).andThrow(re);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(source);


        try
        {
            support.validate(value, resources, fv);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertSame(ex, ve);
        }

        verify();
    }
}
