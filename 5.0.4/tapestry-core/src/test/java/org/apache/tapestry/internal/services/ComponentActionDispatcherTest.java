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

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

import java.io.IOException;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

public class ComponentActionDispatcherTest extends InternalBaseTestCase
{
    @Test
    public void no_dot_or_colon_in_path() throws Exception
    {
        ActionLinkHandler handler = newActionLinkHandler();
        Request request = mockRequest();
        Response response = mockResponse();

        train_getPath(request, "/foo/bar/baz");

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler);

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    protected final ActionLinkHandler newActionLinkHandler()
    {
        return newMock(ActionLinkHandler.class);
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
        test(
                "/foo/MyPage.fred/fee/fie/foe/fum",
                "foo/MyPage",
                "fred",
                TapestryConstants.ACTION_EVENT,
                "fee",
                "fie",
                "foe",
                "fum");
    }

    @Test
    public void default_event_with_context_that_includes_a_colon() throws Exception
    {
        test(
                "/foo/MyPage.underdog/a:b:c/d",
                "foo/MyPage",
                "underdog",
                TapestryConstants.ACTION_EVENT,
                "a:b:c",
                "d");
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
        test(
                "/foo/MyPage.nested:trigger/foo/bar/baz",
                "foo/MyPage",
                "nested",
                "trigger",
                "foo",
                "bar",
                "baz");
    }

    @Test
    public void page_activation_context_in_request() throws Exception
    {
        ActionLinkHandler handler = newActionLinkHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ActionResponseGenerator generator = newMock(ActionResponseGenerator.class);

        train_getPath(request, "/mypage:eventname");

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, "alpha/beta");

        expect(
                handler.handle(
                        eq("mypage"),
                        eq(""),
                        eq("eventname"),
                        aryEq(new String[0]),
                        aryEq(new String[]
                        { "alpha", "beta" }))).andReturn(generator);

        generator.sendClientResponse(response);

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    private void test(String requestPath, String logicalPageName, String nestedComponentId,
            String eventType, String... context) throws IOException
    {
        ActionLinkHandler handler = newActionLinkHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ActionResponseGenerator generator = newMock(ActionResponseGenerator.class);

        train_getPath(request, requestPath);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        expect(
                handler.handle(
                        eq(logicalPageName),
                        eq(nestedComponentId),
                        eq(eventType),
                        aryEq(context),
                        aryEq(new String[0]))).andReturn(generator);

        generator.sendClientResponse(response);

        replay();

        Dispatcher dispatcher = new ComponentActionDispatcher(handler);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }
}
