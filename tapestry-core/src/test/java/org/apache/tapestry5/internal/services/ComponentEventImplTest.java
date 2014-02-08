// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.TapestryMarkers;
import org.apache.tapestry5.internal.structure.ComponentPageElementResources;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

public class ComponentEventImplTest extends InternalBaseTestCase
{

    private ComponentPageElementResources mockResources()
    {
        ComponentPageElementResources resources = newMock(ComponentPageElementResources.class);

        expect(resources.invoke(EasyMock.isA(String.class), EasyMock.isA(Invokable.class))).andAnswer(new IAnswer<Object>()
        {
            public Object answer() throws Throwable
            {
                Invokable inv = (Invokable) EasyMock.getCurrentArguments()[1];

                return inv.invoke();
            }
        });

        return resources;
    }

    @Test
    public void matches_on_event_type()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_getCount(context, 0);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, null, false, model, logger);

        assertTrue(event.matches("eventType", "someId", 0));
        assertFalse(event.matches("foo", "someId", 0));

        verify();
    }

    /**
     * @since 5.2.0
     */
    @Test
    public void no_match_one_event_is_aborted()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();
        Object result = new Object();
        ComponentPageElementResources resources = mockResources();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_handleResult(handler, result, true);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, resources, false, model, logger);

        event.storeResult(result);

        assertFalse(event.matches("eventType", "someId", 0));

        verify();

    }

    @Test
    public void event_type_match_is_case_insensitive()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_getCount(context, 0);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, null, false, model, logger);

        assertTrue(event.matches("EVENTTYPE", "someid", 0));

        verify();
    }

    @Test
    public void matches_on_component_id()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_getCount(context, 0);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, null, false, model, logger);

        assertTrue(event.matches("eventType", "someId", 0));

        assertFalse(event.matches("eventtype", "bar", 0));

        verify();
    }

    @Test
    public void component_id_matches_are_case_insensitive()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_getCount(context, 0);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, null, false, model, logger);

        assertTrue(event.matches("eventtype", "SOMEID", 0));

        verify();
    }

    @Test
    public void coerce_context()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        ComponentPageElementResources resources = mockComponentPageElementResources();
        EventContext context = mockEventContext();
        Integer value = new Integer(27);
        Logger logger = mockLogger();

        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, false);

        train_toClass(resources, "java.lang.Integer", Integer.class);

        train_getCount(context, 2);
        train_get(context, Integer.class, 0, value);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, resources, false, model, logger);

        assertSame(event.coerceContext(0, "java.lang.Integer"), value);

        verify();
    }

    @Test
    public void coerce_when_not_enough_context()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        Logger logger = mockLogger();

        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        train_getCount(context, 0);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, null, false, model, logger);

        event.setMethodDescription("foo.Bar.baz()");

        try
        {
            event.coerceContext(1, "java.lang.Integer");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                    "Method foo.Bar.baz() has more parameters than there are context values for this component event.");
        }

        verify();
    }

    @Test
    public void unable_to_coerce()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        EventContext context = mockEventContext();
        ComponentPageElementResources resources = mockComponentPageElementResources();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        train_toClass(resources, Integer.class.getName(), Integer.class);

        train_getCount(context, 1);

        expect(context.get(Integer.class, 0)).andThrow(new NumberFormatException("Not so easy, is it?"));

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", context, handler, resources, false, model, logger);

        event.setMethodDescription("foo.Bar.baz()");

        try
        {
            event.coerceContext(0, "java.lang.Integer");
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            // Different JVMs will report the conversion error slightly differently,
            // so we don't try to check that part of the error message.

            assertTrue(ex.getMessage().startsWith("Exception in method foo.Bar.baz(), parameter #1:"));
        }

        verify();
    }

    @Test
    public void store_result_and_abort()
    {
        Object result = new Object();
        String methodDescription = "foo.Bar.baz()";
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        EasyMock.expectLastCall().atLeastOnce();

        ComponentPageElementResources resources = mockResources();

        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        ComponentEventCallback handler = mockComponentEventHandler();

        train_handleResult(handler, result, true);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, resources, false, model, logger);

        event.setMethodDescription(methodDescription);

        assertFalse(event.isAborted());

        assertTrue(event.storeResult(result));

        assertTrue(event.isAborted());

        verify();
    }

    @Test
    public void store_result_and_continue()
    {
        Object result = new Object();
        String methodDescription = "foo.Bar.baz()";
        ComponentEventCallback handler = mockComponentEventHandler();
        Logger logger = mockLogger();
        ComponentPageElementResources resources = mockResources();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        train_handleResult(handler, result, false);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, resources, false, model, logger);

        event.setMethodDescription(methodDescription);

        assertFalse(event.storeResult(result));

        assertFalse(event.isAborted());

        verify();
    }

    @Test
    public void store_null_result_does_not_abort_or_invoke_handler()
    {
        ComponentEventCallback handler = mockComponentEventHandler();
        Logger logger = mockLogger();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, null, false, model, logger);

        event.setMethodDescription("foo.Bar.baz()");

        assertFalse(event.storeResult(null));

        assertFalse(event.isAborted());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void store_result_when_aborted_is_failure()
    {
        Object result = new Object();
        ComponentEventCallback handler = mockComponentEventHandler();
        Logger logger = mockLogger();
        ComponentPageElementResources resources = mockResources();
        ComponentModel model = mockComponentModel();

        train_isDebugEnabled(logger, true);
        EasyMock.expectLastCall().atLeastOnce();

        logger.debug(eq(TapestryMarkers.EVENT_HANDLER_METHOD), isA(String.class));

        EasyMock.expectLastCall().atLeastOnce();

        expect(handler.handleResult(result)).andReturn(true);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, resources, false, model, logger);

        event.setMethodDescription("foo.Bar.baz()");
        event.storeResult(result);

        try
        {
            event.setMethodDescription("foo.Bar.biff()");
            event.storeResult(null);
            unreachable();
        } catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(), String.format("Can not store result from invoking method %s, because an event result value has already been obtained from some other event handler method.", "foo.Bar.biff()"));
        }

        verify();
    }
}
