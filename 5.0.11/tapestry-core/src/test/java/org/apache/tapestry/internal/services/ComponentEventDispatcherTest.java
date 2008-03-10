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

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.EmptyEventContext;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.URLEventContext;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class ComponentEventDispatcherTest extends InternalBaseTestCase
{
    private ContextValueEncoder _contextValueEncoder;

    @BeforeClass
    public void setup()
    {
        _contextValueEncoder = getService(ContextValueEncoder.class);
    }

    @Test
    public void no_dot_or_colon_in_path() throws Exception
    {
        ComponentEventRequestHandler handler = newComponentEventRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        RequestEncodingInitializer requestEncodingInitializer = mockRequestEncodingInitializer();

        train_getPath(request, "/foo/bar/baz");

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, null, null, requestEncodingInitializer);

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    protected final RequestEncodingInitializer mockRequestEncodingInitializer()
    {
        return newMock(RequestEncodingInitializer.class);
    }

    protected final ComponentEventRequestHandler newComponentEventRequestHandler()
    {
        return newMock(ComponentEventRequestHandler.class);
    }

    @Test
    public void event_on_page() throws Exception
    {
        test("/foo/MyPage:anevent", "foo/MyPage", "", "anevent");
    }

    /**
     * @see https://issues.apache.org/jira/browse/TAPESTRY-1949
     */
    @Test
    public void event_on_page_with_name_and_dotted_parameters() throws Exception
    {
        test("/foo/MyPage:myevent/1.2.3/4.5.6", "foo/MyPage", "", "myevent", "1.2.3", "4.5.6");
    }

    /**
     * @see https://issues.apache.org/jira/browse/TAPESTRY-1949
     */
    @Test
    public void event_on_page_dotted_parameters() throws Exception
    {
        test("/foo/MyPage:action/1.2.3/4.5.6", "foo/MyPage", "", TapestryConstants.ACTION_EVENT, "1.2.3", "4.5.6");
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
        ComponentEventRequestHandler handler = newComponentEventRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        RequestEncodingInitializer requestEncodingInitializer = mockRequestEncodingInitializer();


        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters("mypage", "mypage", "",
                                                                                                 "eventname",
                                                                                                 new URLEventContext(
                                                                                                         _contextValueEncoder,
                                                                                                         new String[] {
                                                                                                                 "alpha",
                                                                                                                 "beta" }),
                                                                                                 new EmptyEventContext());

        train_getPath(request, "/mypage:eventname");

        train_isPageName(resolver, "mypage", true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, "alpha/beta");

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);

        requestEncodingInitializer.initializeRequestEncoding("mypage");

        handler.handle(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, resolver, _contextValueEncoder,
                                                             requestEncodingInitializer);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void different_active_and_containing_pages() throws Exception
    {
        ComponentEventRequestHandler handler = newComponentEventRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        RequestEncodingInitializer requestEncodingInitializer = mockRequestEncodingInitializer();

        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters("activepage", "mypage",
                                                                                                 "", "eventname",
                                                                                                 new EmptyEventContext(),
                                                                                                 new EmptyEventContext());

        train_getPath(request, "/activepage:eventname");

        train_isPageName(resolver, "activepage", true);

        requestEncodingInitializer.initializeRequestEncoding("activepage");

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, "mypage");

        handler.handle(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, resolver, _contextValueEncoder,
                                                             requestEncodingInitializer);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void request_path_reference_non_existent_page() throws Exception
    {
        ComponentEventRequestHandler handler = newComponentEventRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        RequestEncodingInitializer requestEncodingInitializer = mockRequestEncodingInitializer();

        train_getPath(request, "/mypage.foo");

        train_isPageName(resolver, "mypage", false);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, resolver, null, requestEncodingInitializer);

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    private void test(String requestPath, String containerPageName, String nestedComponentId, String eventType,
                      String... eventContext) throws IOException
    {
        ComponentEventRequestHandler handler = newComponentEventRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        RequestEncodingInitializer requestEncodingInitializer = mockRequestEncodingInitializer();

        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters(containerPageName,
                                                                                                 containerPageName,
                                                                                                 nestedComponentId,
                                                                                                 eventType,
                                                                                                 new EmptyEventContext(),
                                                                                                 new URLEventContext(
                                                                                                         _contextValueEncoder,
                                                                                                         eventContext));

        train_getPath(request, requestPath);

        train_isPageName(resolver, containerPageName, true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);

        requestEncodingInitializer.initializeRequestEncoding(containerPageName);
        
        handler.handle(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, resolver, _contextValueEncoder,
                                                             requestEncodingInitializer);

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }
}
