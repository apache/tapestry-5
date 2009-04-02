// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.*;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PageRenderDispatcherTest extends InternalBaseTestCase
{
    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void not_a_page_request() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        stub_isPageName(resolver, false);

        train_setLocaleFromLocaleName(ls, "foo", false);
        train_getPath(request, "/foo/Bar.baz");

        replay();

        Dispatcher d = new PageRenderDispatcher(null,
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

        assertFalse(d.dispatch(request, response));

        verify();
    }

    // TAPESTRY-1343
    @Test
    public void empty_path() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "");

        train_setLocaleFromLocaleName(ls, "", false);

        train_isPageName(resolver, "", false);

        replay();

        Dispatcher d = new PageRenderDispatcher(null,
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

        assertFalse(d.dispatch(request, response));

        verify();
    }

    @Test
    public void just_the_locale_name() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "/en");

        train_setLocaleFromLocaleName(ls, "en", true);

        train_isPageName(resolver, "", false);

        replay();

        Dispatcher d = new PageRenderDispatcher(null,
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

        assertFalse(d.dispatch(request, response));

        verify();
    }

    /**
     * TAPESTRY-2226
     */
    @Test
    public void page_activation_context_for_root_index_page() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        PageResponseRenderer renderer = mockPageResponseRenderer();
        RequestPageCache cache = mockRequestPageCache();
        ComponentEventResultProcessor processor = newComponentEventResultProcessor();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "/foo/bar");

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/bar", false);
        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "", true);

        train_get(cache, "", page);

        train_getRootElement(page, rootElement);

        train_triggerContextEvent(rootElement, EventConstants.ACTIVATE, new Object[] { "foo", "bar" }, false);

        renderer.renderPageResponse(page);

        replay();

        Dispatcher d = new PageRenderDispatcher(wrap(cache, processor, renderer),
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

        assertTrue(d.dispatch(request, response));

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
        ComponentClassResolver resolver = mockComponentClassResolver();
        PageResponseRenderer renderer = mockPageResponseRenderer();
        RequestPageCache cache = mockRequestPageCache();
        ComponentEventResultProcessor processor = newComponentEventResultProcessor();
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LocalizationSetter ls = mockLocalizationSetter();

        String path = "/foo/Bar" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_get(cache, "foo/Bar", page);
        train_getRootElement(page, rootElement);

        train_triggerContextEvent(rootElement, EventConstants.ACTIVATE, new Object[0], false);

        renderer.renderPageResponse(page);

        replay();

        Dispatcher d = new PageRenderDispatcher(wrap(cache, processor, renderer),
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

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
        ComponentClassResolver resolver = mockComponentClassResolver();
        PageResponseRenderer renderer = mockPageResponseRenderer();
        RequestPageCache cache = mockRequestPageCache();
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LocalizationSetter ls = mockLocalizationSetter();

        String path = "/foo/Bar/zip/zoom" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar/zip/zoom", false);

        train_isPageName(resolver, "foo/Bar/zip", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_get(cache, "foo/Bar", page);
        train_getRootElement(page, rootElement);

        train_triggerContextEvent(rootElement, EventConstants.ACTIVATE, new Object[] { "zip", "zoom" }, false);

        renderer.renderPageResponse(page);

        replay();

        Dispatcher d = new PageRenderDispatcher(wrap(cache, processor, renderer),
                                                new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                                                                                  request, response,
                                                                                  null, null,
                                                                                  null,
                                                                                  true));

        assertTrue(d.dispatch(request, response));

        verify();
    }

    private ComponentRequestHandler wrap(RequestPageCache cache, ComponentEventResultProcessor processor,
                                         PageResponseRenderer renderer)
    {
        PageRenderRequestHandler prh = new PageRenderRequestHandlerImpl(cache, processor, renderer);

        return new ComponentRequestHandlerTerminator(null, prh);
    }

    protected ComponentEventResultProcessor newComponentEventResultProcessor()
    {
        return newMock(ComponentEventResultProcessor.class);
    }

    private void train_triggerContextEvent(ComponentPageElement element, String eventType, final Object[] context,
                                           final boolean handled)
    {
        IAnswer<Boolean> answer = new IAnswer<Boolean>()
        {
            public Boolean answer() throws Throwable
            {
                Object[] arguments = EasyMock.getCurrentArguments();

                EventContext ec = (EventContext) arguments[1];

                assertEquals(ec.getCount(), context.length);

                for (int i = 0; i < context.length; i++)
                {
                    assertEquals(ec.get(Object.class, i), context[i]);
                }


                return handled;
            }
        };

        expect(element.triggerContextEvent(eq(eventType), isA(EventContext.class),
                                           isA(ComponentEventCallback.class))).andAnswer(answer);
    }
}
