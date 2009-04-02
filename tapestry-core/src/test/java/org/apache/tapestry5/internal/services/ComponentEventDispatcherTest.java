// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class ComponentEventDispatcherTest extends InternalBaseTestCase
{
    private ContextValueEncoder contextValueEncoder;

    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        contextValueEncoder = getService(ContextValueEncoder.class);
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void no_dot_or_colon_in_path() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();

        train_getPath(request, "/foo/bar/baz");

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(null,
                                                             new ComponentEventLinkEncoderImpl(null, contextPathEncoder,
                                                                                               null, request, response,
                                                                                               null,
                                                                                               null,
                                                                                               null,
                                                                                               true));

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void event_on_page() throws Exception
    {
        test("/foo/MyPage:anevent", "foo", "foo/MyPage", "", "anevent");
    }

    /**
     * @see {@link https://issues.apache.org/jira/browse/TAPESTRY-1949}
     */
    @Test
    public void event_on_page_with_name_and_dotted_parameters() throws Exception
    {
        test("/foo/MyPage:myevent/1.2.3/4.5.6", "foo", "foo/MyPage", "", "myevent", "1.2.3", "4.5.6");
    }

    /**
     * @see https://issues.apache.org/jira/browse/TAPESTRY-1949
     */
    @Test
    public void event_on_page_dotted_parameters() throws Exception
    {
        test("/foo/MyPage:action/1.2.3/4.5.6", "foo", "foo/MyPage", "", EventConstants.ACTION, "1.2.3", "4.5.6");
    }

    @Test
    public void event_on_component_within_page() throws Exception
    {
        test("/foo/MyPage.fred:anevent", "foo", "foo/MyPage", "fred", "anevent");
    }

    @Test
    public void default_event_with_nested_id() throws Exception
    {
        test("/foo/MyPage.fred", "foo", "foo/MyPage", "fred", EventConstants.ACTION);
    }

    @Test
    public void default_event_with_nested_id_and_context() throws Exception
    {
        test("/foo/MyPage.fred/fee/fie/foe/fum", "foo", "foo/MyPage", "fred", EventConstants.ACTION, "fee", "fie",
             "foe", "fum");
    }

    @Test
    public void default_event_with_context_that_includes_a_colon() throws Exception
    {
        test("/foo/MyPage.underdog/a:b:c/d", "foo", "foo/MyPage", "underdog", EventConstants.ACTION, "a:b:c", "d");
    }

    @Test
    public void event_on_nested_component_within_page() throws Exception
    {
        test("/foo/MyPage.barney.fred:anevent", "foo", "foo/MyPage", "barney.fred", "anevent");
    }

    @Test
    public void page_event_with_context() throws Exception
    {
        test("/foo/MyPage:trigger/foo", "foo", "foo/MyPage", "", "trigger", "foo");
    }

    @Test
    public void nested_component_event_with_context() throws Exception
    {
        test("/foo/MyPage.nested:trigger/foo/bar/baz", "foo", "foo/MyPage", "nested", "trigger", "foo", "bar", "baz");
    }

    @Test
    public void page_activation_context_in_request() throws Exception
    {
        ComponentRequestHandler handler = mockComponentRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        LocalizationSetter ls = mockLocalizationSetter();

        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters("mypage", "mypage", "",
                                                                                                 "eventname",
                                                                                                 new URLEventContext(
                                                                                                         contextValueEncoder,
                                                                                                         new String[] {
                                                                                                                 "alpha",
                                                                                                                 "beta" }),
                                                                                                 new EmptyEventContext());

        train_getPath(request, "/mypage:eventname");

        train_setLocaleFromLocaleName(ls, "", false);

        train_isPageName(resolver, "mypage", true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, "alpha/beta");

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);

        handler.handleComponentEvent(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, new ComponentEventLinkEncoderImpl(resolver,
                                                                                                        contextPathEncoder,
                                                                                                        ls, request,
                                                                                                        response,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        true));

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void different_active_and_containing_pages() throws Exception
    {
        ComponentRequestHandler handler = mockComponentRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        LocalizationSetter ls = mockLocalizationSetter();

        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters("activepage", "mypage",
                                                                                                 "", "eventname",
                                                                                                 new EmptyEventContext(),
                                                                                                 new EmptyEventContext());

        train_getPath(request, "/activepage:eventname");

        train_setLocaleFromLocaleName(ls, "", false);

        train_isPageName(resolver, "activepage", true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, "mypage");

        handler.handleComponentEvent(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(handler, new ComponentEventLinkEncoderImpl(resolver,
                                                                                                        contextPathEncoder,
                                                                                                        ls, request,
                                                                                                        response,
                                                                                                        null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        true));

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }

    @Test
    public void request_path_reference_non_existent_page() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "/en/mypage.foo");

        train_setLocaleFromLocaleName(ls, "en", true);
        train_isPageName(resolver, "mypage", false);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(null, new ComponentEventLinkEncoderImpl(resolver,
                                                                                                     contextPathEncoder,
                                                                                                     ls, request,
                                                                                                     response,
                                                                                                     null,
                                                                                                     null,
                                                                                                     null,
                                                                                                     true));

        assertFalse(dispatcher.dispatch(request, response));

        verify();
    }

    private void test(String requestPath, String localeName, String containerPageName, String nestedComponentId,
                      String eventType,
                      String... eventContext) throws IOException
    {
        ComponentRequestHandler handler = mockComponentRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentClassResolver resolver = mockComponentClassResolver();
        LocalizationSetter localizationSetter = mockLocalizationSetter();

        ComponentEventRequestParameters expectedParameters = new ComponentEventRequestParameters(containerPageName,
                                                                                                 containerPageName,
                                                                                                 nestedComponentId,
                                                                                                 eventType,
                                                                                                 new EmptyEventContext(),
                                                                                                 new URLEventContext(
                                                                                                         contextValueEncoder,
                                                                                                         eventContext));

        train_getPath(request, requestPath);

        train_setLocaleFromLocaleName(localizationSetter, localeName, false);

        train_isPageName(resolver, containerPageName, true);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);

        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);

        handler.handleComponentEvent(expectedParameters);

        replay();

        Dispatcher dispatcher = new ComponentEventDispatcher(
                handler, new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, localizationSetter, request,
                                                           response, null, null,
                                                           null,
                                                           true));

        assertTrue(dispatcher.dispatch(request, response));

        verify();
    }
}
