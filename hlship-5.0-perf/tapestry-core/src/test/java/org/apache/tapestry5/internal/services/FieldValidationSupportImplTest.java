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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.corelib.internal.InternalMessages;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ValidationMessagesSource;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FieldValidationSupportImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }


    @SuppressWarnings({"unchecked"})
    @Test
    public void parse_client_via_event() throws ValidationException
    {
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

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

                return handler.handleResult(context[0].toString().toUpperCase());
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.PARSE_CLIENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();


        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        Object actual = support.parseClient(clientValue, resources, translator, nullFieldStrategy);

        assertEquals(actual, clientValue.toUpperCase());

        verify();
    }

    @Test
    public void parse_client_for_null_value_returns_null_and_bypasses_events_and_translator() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        String clientValue = "";

        train_replaceFromClient(nullFieldStrategy, "");

        ignoreEvent(resources, EventConstants.PARSE_CLIENT, clientValue);

        expect(translator.parse(clientValue)).andReturn("");

        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        Object actual = support.parseClient(clientValue, resources, translator, nullFieldStrategy);

        assertEquals(actual, "");

        verify();
    }

    private void ignoreEvent(ComponentResources resources, String event, Object... context)
    {
        EasyMock.expect(resources.triggerEvent(EasyMock.eq(event),
                                               EasyMock.aryEq(context),
                                               EasyMock.isA(ComponentEventCallback.class))).andReturn(false);
    }

    protected final void train_replaceFromClient(NullFieldStrategy nullFieldStrategy, String value)
    {
        expect(nullFieldStrategy.replaceFromClient()).andReturn(value).atLeastOnce();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void parse_client_event_handler_throws_validation_exception() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        ValidationException ve = new ValidationException("Just didn't feel right.");
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        String clientValue = "abracadabra";

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.PARSE_CLIENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andThrow(
                new RuntimeException(ve));


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        try
        {
            support.parseClient(clientValue, resources, translator, nullFieldStrategy);

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
        FieldTranslator translator = mockFieldTranslator();
        RuntimeException re = new RuntimeException("Just didn't feel right.");
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        String clientValue = "abracadabra";


        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.PARSE_CLIENT),
                                               EasyMock.isA(Object[].class),
                                               EasyMock.isA(ComponentEventCallback.class))).andThrow(re);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        try
        {
            support.parseClient(clientValue, resources, translator, nullFieldStrategy);

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
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        ValidationMessagesSource source = mockValidationMessagesSource();
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        String clientValue = "abracadabra";

        ignoreEvent(resources, EventConstants.PARSE_CLIENT, clientValue);

        expect(translator.parse(clientValue)).andReturn("foobar");

        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        Object actual = support.parseClient(clientValue, resources, translator, nullFieldStrategy);

        assertEquals(actual, "foobar");

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void to_client_via_translator()
    {
        Object value = new Integer(99);
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        expect(translator.getType()).andReturn(Integer.class);

        String clientValue = "abracadabra";

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.TO_CLIENT),
                                               EasyMock.aryEq(new Object[] {value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andReturn(false);

        expect(translator.toClient(value)).andReturn(clientValue);

        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        String actual = support.toClient(value, resources, translator, nullFieldStrategy);

        assertEquals(actual, clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void to_client_via_event_handler() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        NullFieldStrategy nullFieldStrategy = mockNullFieldStrategy();

        final String clientValue = "abracadabra";

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventCallback handler = (ComponentEventCallback) args[2];

                return handler.handleResult(clientValue);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.TO_CLIENT),
                                               EasyMock.aryEq(new Object[] {value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(null);

        String actual = support.toClient(value, resources, translator, nullFieldStrategy);

        assertEquals(actual, clientValue);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    public void to_client_via_event_handler_returns_non_string() throws Exception
    {
        Object value = new Object();
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();

        IAnswer answer = new IAnswer()
        {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();
                ComponentEventCallback handler = (ComponentEventCallback) args[2];

                // Return an innappropriate value.

                return handler.handleResult(this);
            }
        };

        EasyMock.expect(resources.triggerEvent(EasyMock.eq(EventConstants.TO_CLIENT),
                                               EasyMock.aryEq(new Object[] {value}),
                                               EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(null);

        try
        {

            support.toClient(value, resources, translator, null);

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

        Object value = new Object();

        fv.validate(value);

        ComponentEventCallback handler = null;

        expect(resources.triggerEvent(EasyMock.eq(EventConstants.VALIDATE),
                                      EasyMock.aryEq(new Object[] {value}), EasyMock.eq(handler))).andReturn(true);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);

        support.validate(value, resources, fv);

        verify();
    }

    @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
    @Test
    public void event_trigger_throws_validation_exception() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        FieldValidator fv = mockFieldValidator();

        Object value = new Object();

        ValidationException ve = new ValidationException("Bah!");
        RuntimeException re = new RuntimeException(ve);

        ComponentEventCallback handler = null;

        fv.validate(value);

        expect(resources.triggerEvent(eq(EventConstants.VALIDATE),
                                      EasyMock.aryEq(new Object[] {value}), eq(handler))).andThrow(re);


        replay();

        FieldValidationSupport support = new FieldValidationSupportImpl(typeCoercer);


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
