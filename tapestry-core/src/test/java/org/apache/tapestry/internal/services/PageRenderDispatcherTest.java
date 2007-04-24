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

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

public class PageRenderDispatcherTest extends InternalBaseTestCase
{
    @Test
    public void not_a_page_request() throws Exception
    {
        ComponentClassResolver resolver = newComponentClassResolver();
        PageResponseRenderer renderer = newPageResponseRenderer();
        RequestPageCache cache = newRequestPageCache();
        PageLinkHandler handler = new PageLinkHandlerImpl(cache, null);
        Request request = newRequest();
        Response response = newResponse();

        stub_isPageName(resolver, false);

        train_getPath(request, "/foo/Bar.baz");

        replay();

        Dispatcher d = new PageRenderDispatcher(resolver, handler, renderer);

        assertFalse(d.dispatch(request, response));

        verify();
    }

    // TAPESTRY-1343
    @Test
    public void empty_path() throws Exception
    {
        ComponentClassResolver resolver = newComponentClassResolver();
        PageResponseRenderer renderer = newPageResponseRenderer();
        PageLinkHandler handler = newMock(PageLinkHandler.class);
        Request request = newRequest();
        Response response = newResponse();

        train_getPath(request, "");

        replay();

        Dispatcher d = new PageRenderDispatcher(resolver, handler, renderer);

        assertFalse(d.dispatch(request, response));

        verify();
    }

    @Test
    public void no_extra_context_without_final_slash() throws Exception
    {
        no_extra_context(false);
    }

    @Test
    public void no_extra_context_with_final_slash() throws Exception
    {
        no_extra_context(true);
    }

    private void no_extra_context(boolean finalSlash) throws Exception
    {
        ComponentClassResolver resolver = newComponentClassResolver();
        PageResponseRenderer renderer = newPageResponseRenderer();
        RequestPageCache cache = newRequestPageCache();
        ComponentEventResultProcessor processor = newComponentEventResultProcessor();
        Request request = newRequest();
        Response response = newResponse();
        Page page = newPage();
        ComponentPageElement rootElement = newComponentPageElement();

        String path = "/foo/Bar" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "foo/Bar", true);

        train_get(cache, "foo/Bar", page);
        train_getRootElement(page, rootElement);

        train_triggerEvent(
                rootElement,
                TapestryConstants.ACTIVATE_EVENT,
                new Object[0],
                null,
                false);

        renderer.renderPageResponse(page, response);

        replay();

        PageLinkHandler handler = new PageLinkHandlerImpl(cache, processor);

        Dispatcher d = new PageRenderDispatcher(resolver, handler, renderer);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void context_passed_in_path_without_final_slash() throws Exception
    {
        context_passed_in_path(false);
    }

    @Test
    public void context_passed_in_path_with_final_slash() throws Exception
    {
        context_passed_in_path(true);
    }

    private void context_passed_in_path(boolean finalSlash) throws Exception
    {
        ComponentEventResultProcessor processor = newComponentEventResultProcessor();
        ComponentClassResolver resolver = newComponentClassResolver();
        PageResponseRenderer renderer = newPageResponseRenderer();
        RequestPageCache cache = newRequestPageCache();
        Request request = newRequest();
        Response response = newResponse();
        Page page = newPage();
        ComponentPageElement rootElement = newComponentPageElement();

        String path = "/foo/Bar/zip/zoom" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "foo/Bar", true);

        train_get(cache, "foo/Bar", page);
        train_getRootElement(page, rootElement);

        train_triggerEvent(rootElement, TapestryConstants.ACTIVATE_EVENT, new Object[]
        { "zip", "zoom" }, null, false);

        renderer.renderPageResponse(page, response);

        replay();

        PageLinkHandler handler = new PageLinkHandlerImpl(cache, processor);

        Dispatcher d = new PageRenderDispatcher(resolver, handler, renderer);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    protected ComponentEventResultProcessor newComponentEventResultProcessor()
    {
        return newMock(ComponentEventResultProcessor.class);
    }

    private void train_triggerEvent(ComponentPageElement element, String eventType,
            Object[] context, ComponentEventHandler handler, boolean handled)
    {
        expect(
                element.triggerEvent(
                        eq(eventType),
                        aryEq(context),
                        isA(ComponentEventHandler.class))).andReturn(handled);
    }
}
