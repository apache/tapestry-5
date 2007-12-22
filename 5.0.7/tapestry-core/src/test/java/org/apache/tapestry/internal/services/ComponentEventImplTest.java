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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentEvent;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ComponentEventImplTest extends InternalBaseTestCase
{
    private TypeCoercer _coercer;

    @BeforeClass
    public void setup_coercer()
    {
        _coercer = getObject(TypeCoercer.class, null);
    }

    @AfterClass
    public void cleanup_coercer()
    {
        _coercer = null;
    }

    @Test
    public void matches_on_event_type()
    {
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        assertTrue(event.matches("eventType", "someId", 0));
        assertFalse(event.matches("foo", "someId", 0));

        verify();
    }

    @Test
    public void event_type_match_is_case_insensitive()
    {
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        assertTrue(event.matches("EVENTTYPE", "someid", 0));

        verify();
    }

    @Test
    public void matches_on_component_id()
    {
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        assertTrue(event.matches("eventType", "someId", 0));

        assertFalse(event.matches("eventtype", "bar", 0));

        verify();
    }

    @Test
    public void component_id_matches_are_case_insensitive()
    {
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();
        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        assertTrue(event.matches("eventtype", "SOMEID", 0));

        verify();
    }

    @Test
    public void coerce_context()
    {
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", new String[]{"27"}, handler, _coercer,
                                                      null);

        assertEquals(event.coerceContext(0, "java.lang.Integer"), new Integer(27));

        verify();
    }

    @Test
    public void coerce_when_not_enough_context()
    {
        ComponentEventHandler handler = mockComponentEventHandler();
        Component component = mockComponent();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", new String[]{"27"}, handler, _coercer,
                                                      null);

        event.setSource(component, "foo.Bar.baz()");

        try
        {
            event.coerceContext(1, "java.lang.Integer");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Method foo.Bar.baz() has more parameters than there are context values for this component event.");
        }

        verify();
    }

    @Test
    public void unable_to_coerce()
    {
        ComponentEventHandler handler = mockComponentEventHandler();
        Component component = mockComponent();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", new String[]{"abc"}, handler, _coercer,
                                                      null);

        event.setSource(component, "foo.Bar.baz()");

        try
        {
            event.coerceContext(0, "java.lang.Integer");
            unreachable();
        }
        catch (IllegalArgumentException ex)
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
        Component component = mockComponent();

        ComponentEventHandler handler = mockComponentEventHandler();

        train_handleResult(handler, result, component, methodDescription, true);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        event.setSource(component, methodDescription);

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
        Component component = mockComponent();
        ComponentEventHandler handler = mockComponentEventHandler();

        train_handleResult(handler, result, component, methodDescription, false);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        event.setSource(component, methodDescription);

        assertFalse(event.storeResult(result));

        assertFalse(event.isAborted());

        verify();
    }

    @Test
    public void store_null_result_does_not_abort_or_invoke_handler()
    {
        Component component = mockComponent();
        ComponentEventHandler handler = mockComponentEventHandler();

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        event.setSource(component, "foo.Bar.baz()");

        assertFalse(event.storeResult(null));

        assertFalse(event.isAborted());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void store_result_when_aborted_is_failure()
    {
        Object result = new Object();
        ComponentEventHandler handler = mockComponentEventHandler();
        Component component = mockComponent();

        expect(handler.handleResult(result, component, "foo.Bar.baz()")).andReturn(true);

        replay();

        ComponentEvent event = new ComponentEventImpl("eventType", "someId", null, handler, _coercer, null);

        event.setSource(component, "foo.Bar.baz()");
        event.storeResult(result);

        try
        {
            event.setSource(component, "foo.Bar.biff()");
            event.storeResult(null);
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages
                    .componentEventIsAborted("foo.Bar.biff()"));
        }

        verify();
    }
}
