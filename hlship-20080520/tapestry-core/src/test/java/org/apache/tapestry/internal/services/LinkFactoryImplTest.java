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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentEventCallback;
import org.apache.tapestry.EventConstants;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.services.ContextValueEncoder;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkFactoryImplTest extends InternalBaseTestCase
{
    private static final String ENCODED = "*encoded*";

    private static final String PAGE_LOGICAL_NAME = "sub/MyPage";

    private ContextValueEncoder contextValueEncoder;

    @BeforeClass
    public void setup()
    {
        contextValueEncoder = getObject(ContextValueEncoder.class, null);
    }

    @AfterClass
    public void cleanup()
    {
        contextValueEncoder = null;
    }

    @Test
    public void action_link_root_context_no_ids()
    {
        testActionLink("", PAGE_LOGICAL_NAME, "foo.bar", "someaction", "/sub/mypage.foo.bar:someaction");

    }

    @Test
    public void action_link_root_context_with_ids()
    {
        testActionLink("", PAGE_LOGICAL_NAME, "foo.bar", "publish", "/sub/mypage.foo.bar:publish/fred/5", "fred", 5);
    }

    @Test
    public void action_link_with_default_action()
    {
        testActionLink("", PAGE_LOGICAL_NAME, "foo.bar", EventConstants.ACTION, "/sub/mypage.foo.bar/fred/5",
                       "fred", 5);
    }

    @Test
    public void page_level_event_always_includes_action()
    {
        testActionLink("", PAGE_LOGICAL_NAME, "", EventConstants.ACTION, "/sub/mypage:action/barney/99",
                       "barney", 99);
    }

    @Test
    public void action_link_named_context_no_ids()
    {
        testActionLink("/fred", PAGE_LOGICAL_NAME, "foo.bar", "someaction", "/fred/sub/mypage.foo.bar:someaction");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_getBaseURL(securityManager, page, null);


        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));


        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, optimizer, null, contextValueEncoder,
                                                  securityManager);

        factory.addListener(listener);

        Link link = factory.createPageLink(page, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @Test
    public void secure_page_link()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);


        train_getBaseURL(securityManager, page, "https://example.org");


        train_encodeRedirectURL(response,
                                "https://example.org/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar",
                                ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, optimizer, null,
                                                  contextValueEncoder,
                                                  securityManager);

        factory.addListener(listener);

        Link link = factory.createPageLink(page, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link_using_supplied_activation_context()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        Holder<Link> holder = new Holder<Link>();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        train_getBaseURL(securityManager, page, null);


        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/biff/bazz", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, optimizer, null, contextValueEncoder,
                                                  securityManager);
        factory.addListener(listener);

        Link link = factory.createPageLink(page, false, "biff", "bazz");

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link_using_empty_activation_context_and_override()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        Holder<Link> holder = new Holder<Link>();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        train_getBaseURL(securityManager, page, null);

        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase(), ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, optimizer, null, contextValueEncoder,
                                                  securityManager);
        factory.addListener(listener);

        Link link = factory.createPageLink(page, true);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @Test
    public void page_link_by_name()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        train_get(cache, PAGE_LOGICAL_NAME, page);

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.
        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_getBaseURL(securityManager, page, null);

        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, null, contextValueEncoder,
                                                  securityManager);
        factory.addListener(listener);

        Link link = factory.createPageLink(PAGE_LOGICAL_NAME, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @Test
    public void page_link_for_root_index()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        String logicalName = "Index";

        train_get(cache, logicalName, page);

        train_getLogicalName(page, logicalName);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.
        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_getBaseURL(securityManager, page, null);

        train_encodeRedirectURL(response, "/barney/foo/bar", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, null, contextValueEncoder,
                                                  securityManager);
        factory.addListener(listener);

        Link link = factory.createPageLink(logicalName, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @Test
    public void page_link_for_index_in_folder()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        String logicalName = "mylib/stuff/Index";

        train_get(cache, logicalName, page);

        train_getLogicalName(page, logicalName);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.
        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_getBaseURL(securityManager, page, null);

        train_encodeRedirectURL(response, "/barney/mylib/stuff/foo/bar", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, null, contextValueEncoder,
                                                  securityManager);
        factory.addListener(listener);

        Link link = factory.createPageLink(logicalName, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    private void train_triggerPassivateEventForPageLink(ComponentPageElement rootElement, LinkFactoryListener listener,
                                                        Holder<Link> holder)
    {
        IAnswer<Boolean> triggerEventAnswer = newAnswerForPassivateEventTrigger();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(rootElement.triggerEvent(eq(EventConstants.PASSIVATE), (Object[]) isNull(),
                                        isA(ComponentEventCallback.class))).andAnswer(triggerEventAnswer);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);
    }

    @SuppressWarnings("unchecked")
    private void train_triggerPassivateEventForActionLink(ComponentPageElement rootElement,
                                                          LinkFactoryListener listener, Holder<Link> holder)
    {
        IAnswer<Boolean> triggerEventAnswer = newAnswerForPassivateEventTrigger();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(rootElement.triggerEvent(eq(EventConstants.PASSIVATE), (Object[]) isNull(),
                                        isA(ComponentEventCallback.class))).andAnswer(triggerEventAnswer);

        listener.createdActionLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);
    }

    private IAnswer<Void> newAnswerForCreatedLink(final Holder<Link> holder)
    {
        return new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                Link link = (Link) EasyMock.getCurrentArguments()[0];

                holder.put(link);

                return null;
            }
        };
    }

    private IAnswer<Boolean> newAnswerForPassivateEventTrigger()
    {
        return new IAnswer<Boolean>()
        {
            @SuppressWarnings("unchecked")
            public Boolean answer() throws Throwable
            {
                ComponentEventCallback handler = (ComponentEventCallback) EasyMock
                        .getCurrentArguments()[2];

                handler.handleResult(new Object[] { "foo", "bar" });

                return true;
            }
        };
    }

    @Test
    public void action_with_context_that_contains_periods()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        PageRenderQueue queue = mockPageRenderQueue();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        String optimizedPath = "/optimized/path";

        final Holder<Link> holder = new Holder<Link>();

        train_getLogicalName(page, "mypage");
        train_getContextPath(request, "");

        train_getBaseURL(securityManager, page, null);

        train_optimizePath(optimizer, "/mypage:myaction/1.2.3/4.5.6?t:ac=foo/bar", optimizedPath);

        train_getRootElement(page, rootElement);
        train_triggerPassivateEventForActionLink(rootElement, listener, holder);

        train_getRenderingPage(queue, page);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        train_encodeURL(response, "/optimized/path", ENCODED);

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, queue,
                                                  contextValueEncoder, securityManager);
        factory.addListener(listener);

        Link link = factory.createActionLink(page, null, "myaction", false, "1.2.3", "4.5.6");

        assertEquals(link.toURI(), ENCODED);
        assertSame(link, holder.get());

        verify();
    }

    /**
     * For good measure, we're going to also test a security change.
     */
    @Test
    public void action_for_non_active_page()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page containingPage = mockPage();
        Page activePage = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        PageRenderQueue queue = mockPageRenderQueue();
        RequestSecurityManager securityManager = mockRequestSecurityManager();

        final Holder<Link> holder = new Holder<Link>();

        train_getLogicalName(containingPage, "MyPage");
        train_getContextPath(request, "");


        train_getRootElement(activePage, rootElement);
        train_triggerPassivateEventForActionLink(rootElement, listener, holder);

        train_getRenderingPage(queue, activePage);

        train_getLogicalName(activePage, "ActivePage");

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        train_getBaseURL(securityManager, activePage, "http://example.org");

        train_encodeURL(response, "http://example.org/activepage:myaction?t:ac=foo/bar&t:cp=mypage", ENCODED);

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, queue,
                                                  contextValueEncoder, securityManager);
        factory.addListener(listener);

        Link link = factory.createActionLink(containingPage, null, "myaction", false);

        assertEquals(link.toURI(), ENCODED);
        assertSame(link, holder.get());

        verify();
    }


    @SuppressWarnings("unchecked")
    private void testActionLink(String contextPath, String logicalPageName, String nestedId, String eventName,
                                String expectedURI, Object... context)
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentPageElement rootElement = mockComponentPageElement();
        Page page = mockPage();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        PageRenderQueue queue = mockPageRenderQueue();
        RequestSecurityManager securityManager = mockRequestSecurityManager();


        String optimizedPath = "/optimized/path";

        String generatedPath = String.format("%s?%s=foo/bar", expectedURI, InternalConstants.PAGE_CONTEXT_NAME);

        final Holder<Link> holder = new Holder<Link>();

        train_getLogicalName(page, logicalPageName);
        train_getContextPath(request, contextPath);

        train_optimizePath(optimizer, generatedPath, optimizedPath);

        train_getRootElement(page, rootElement);
        train_triggerPassivateEventForActionLink(rootElement, listener, holder);

        train_getRenderingPage(queue, page);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocationImpl.class));

        train_getBaseURL(securityManager, page, null);

        train_encodeURL(response, optimizedPath, ENCODED);

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, optimizer, queue,
                                                  contextValueEncoder, securityManager);
        factory.addListener(listener);

        Link link = factory.createActionLink(page, nestedId, eventName, false, context);

        assertEquals(link.toURI(), ENCODED);
        assertSame(link, holder.get());

        verify();
    }

}
