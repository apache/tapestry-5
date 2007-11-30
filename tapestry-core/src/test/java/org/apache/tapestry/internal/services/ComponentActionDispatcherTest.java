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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.*;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import org.testng.annotations.Test;

import java.io.IOException;

public class ComponentActionDispatcherTest extends InternalBaseTestCase
{
    @Test
    public void no_dot_or_colon_in_path() throws Exception
    {
        ComponentActionRequestHandler handler = newComponentActionRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();

        train_getPath(request, "/foo/bar/baz");

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler, null);

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    protected final ComponentActionRequestHandler newComponentActionRequestHandler()
    {
        return newMock(ComponentActionRequestHandler.class);
    }

    @Test
    public void event_on_page() throws Exception
    {
        test("/foo/MyPage:anevent", "foo/MyPage", "", "anevent");
    }

    @Test
    public void event_on_component_within_page() throws Exception
    {
        test("/foo/MyPage.fred:anevent", "foo/MyPage", "fred", "anevent");
    }

    @Test
    public void default_event_with_nested_id() throws Exception
    {
        test("/foo/MyPage.fred", "foo/MyPage", "fred", TapestryConstants.ACTION_EVENT);
    }

    @Test
    public void default_event_with_nested_id_and_context() throws Exception
    {
        test("/foo/MyPage.fred/fee/fie/foe/fum", "foo/MyPage", "fred", TapestryConstants.ACTION_EVENT, "fee", "fie",
             "foe", "fum");
    }

    @Test
    public void default_event_with_context_that_includes_a_colon() throws Exception
    {
        test("/foo/MyPage.underdog/a:b:c/d", "foo/MyPage", "underdog", TapestryConstants.ACTION_EVENT, "a:b:c", "d");
    }

    @Test
    public void event_on_nested_component_within_page() throws Exception
    {
        test("/foo/MyPage.barney.fred:anevent", "foo/MyPage", "barney.fred", "anevent");
    }

    @Test
    public void page_event_with_context() throws Exception
    {
        test("/foo/MyPage:trigger/foo", "foo/MyPage", "", "trigger", "foo");
    }

    @Test
    public void nested_component_event_with_context() throws Exception
    {
        test("/foo/MyPage.nested:trigger/foo/bar/baz", "foo/MyPage", "nested", "trigger", "foo", "bar", "baz");
    }

    @Test
    public void page_activation_context_in_request() throws Exception
    {
        ComponentActionRequestHandler handler = newComponentActionRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getPath(request, "/mypage:eventname");

        train_isPageName(resolver, "mypage", true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, "alpha/beta");

        handler.handle(eq("mypage"), eq(""), eq("eventname"), aryEq(new String[0]),
                       aryEq(new String[]{"alpha", "beta"}));

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler, resolver);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void request_path_reference_non_existent_page() throws Exception
    {
        ComponentActionRequestHandler handler = newComponentActionRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getPath(request, "/mypage.foo");

        train_isPageName(resolver, "mypage", false);

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler, resolver);

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    private void test(String requestPath, String logicalPageName, String nestedComponentId, String eventType,
                      String... context) throws IOException
    {
        ComponentActionRequestHandler handler = newComponentActionRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getPath(request, requestPath);

        train_isPageName(resolver, logicalPageName, true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        handler.handle(eq(logicalPageName), eq(nestedComponentId), eq(eventType), aryEq(context), aryEq(new String[0]));

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler, resolver);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }
}
