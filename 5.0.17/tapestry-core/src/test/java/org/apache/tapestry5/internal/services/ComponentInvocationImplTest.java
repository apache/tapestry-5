// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ComponentInvocationImplTest extends InternalBaseTestCase
{
    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void no_event_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"), null,
                                                                     null, false);
        assertEquals(invocation.buildURI(), "abc");

        assertEquals(invocation.getEventContext().getCount(), 0);
    }

    @Test
    public void event_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {"x", 123}, null, false);
        assertEquals(invocation.buildURI(), "abc/x/123");
    }

    @Test
    public void event_context_and_parameters()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {"x", 123}, null, false);
        invocation.addParameter("p1", "foo");
        invocation.addParameter("p2", "bar");

        assertEquals(invocation.buildURI(), "abc/x/123?p1=foo&p2=bar");

        assertListsEquals(invocation.getParameterNames(), "p1", "p2");

        assertEquals(invocation.getParameterValue("p1"), "foo");
        assertEquals(invocation.getParameterValue("p2"), "bar");
    }

    @Test
    public void uri_does_not_include_parameters_if_for_form()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {"x", 123}, null, true);
        invocation.addParameter("p1", "foo");
        invocation.addParameter("p2", "bar");

        assertEquals(invocation.buildURI(), "abc/x/123");
    }

    @Test
    public void adding_duplicate_parmaeter_is_an_error()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {"x", 123}, null, true);
        invocation.addParameter("p1", "foo");

        try
        {
            invocation.addParameter("p1", "bar");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Parameter names are required to be unique.  Parameter 'p1' already has the value 'foo'.");
        }
    }

    @Test
    public void get_unknown_parameter_returns_null()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {"x", 123}, null, true);
        invocation.addParameter("p1", "foo");


        assertNull(invocation.getParameterValue("xyz"));
    }

    @Test
    public void page_render_invocation_with_no_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new PageRenderTarget("admin/Menu"),
                                                                     null, null, false);

        assertEquals(invocation.buildURI(), "admin/menu");

        assertEquals(invocation.getPageActivationContext().getCount(), 0);
    }

    @Test
    public void page_render_invocation_with_context()
    {
        PageRenderTarget target = new PageRenderTarget("admin/Menu");

        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     target,
                                                                     null,
                                                                     new Object[] {1, 2, null, "why?"}, false);

        assertEquals(invocation.buildURI(), "admin/menu/1/2/$N/why$003f");
        assertSame(invocation.getTarget(), target);
    }

    @Test
    public void event_invocation_with_action_and_page_activation_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {1, 2},
                                                                     new Object[] {3, 4},
                                                                     false);

        assertEquals(invocation.buildURI(), "abc/1/2?t:ac=3/4");
    }

    @Test
    public void get_event_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     new Object[] {1, 2, null, "", "hey!"},
                                                                     new Object[] {3, 4},
                                                                     false);

        EventContext ec = invocation.getEventContext();

        assertEquals(ec.getCount(), 5);
        assertEquals(ec.get(Integer.class, 0), new Integer(1));
        assertEquals(ec.get(String.class, 4), "hey!");
    }

    @Test
    public void get_page_activation_context()
    {
        ComponentInvocation invocation = new ComponentInvocationImpl(contextPathEncoder,
                                                                     new OpaqueConstantTarget("abc"),
                                                                     null,
                                                                     new Object[] {1, 2, null, "", "hey!"},
                                                                     true);

        EventContext ec = invocation.getPageActivationContext();

        assertEquals(ec.getCount(), 5);
        assertEquals(ec.get(Integer.class, 0), new Integer(1));
        assertEquals(ec.get(String.class, 4), "hey!");
    }
}
