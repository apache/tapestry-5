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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.easymock.Capture;
import static org.easymock.EasyMock.capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkFactoryImplTest extends InternalBaseTestCase
{
    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void create_page_render_link()
    {
        testPageLinkCreation("order/Edit", "/context/order/edit", false);
    }

    @Test
    public void create_page_render_link_for_index_page()
    {
        testPageLinkCreation("order/Index", "/context/order", false);
    }

    @Test
    public void create_page_render_link_for_index_page_with_context()
    {
        testPageLinkCreation("order/Index", "/context/order/99", false, 99);
    }

    @Test
    public void create_page_render_link_to_root_index_page()
    {
        testPageLinkCreation("Index", "/context", false);
    }

    @Test
    public void creat_page_render_link_to_root_index_page_with_context()
    {
        testPageLinkCreation("Index", "/context/202", false, 202);
    }


    @Test
    public void create_page_render_link_with_override_event_context()
    {
        testPageLinkCreation("order/Edit", "/context/order/edit/1/2", true, 1, 2);
    }

    @Test
    public void create_page_render_link_with_event_context_from_passivate()
    {
        testPageLinkCreation("order/Edit", "/context/order/edit/from/passivate", false, "from", "passivate");
    }

    @Test
    public void create_page_render_link_by_name()
    {
        String logicalName = "order/Edit";

        String expectedURL = "/base/context/order/edit/3";
        String encodedURL = "encoded:" + expectedURL;

        Page page = mockPage();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        RequestSecurityManager securityManager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentInvocationMap invocationMap = new NoOpComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        RequestPageCache pageCache = mockRequestPageCache();

        train_get(pageCache, logicalName, page);

        train_getLogicalName(page, logicalName);

        train_collectPageActivationContext(collector, page, 3);

        train_getBaseURL(securityManager, page, "/base");
        train_getContextPath(request, "/context");

        train_encodeURL(response, expectedURL, encodedURL);

        replay();


        LinkFactory factory = new LinkFactoryImpl(request, response, invocationMap, pageCache, optimizer, null,
                                                  securityManager, contextPathEncoder, collector);


        Link link = factory.createPageRenderLink(logicalName, false);

        // Make sure the same link is returned.

        assertEquals(link.toURI(), encodedURL);

        verify();
    }

    private void testPageLinkCreation(String logicalName, String expectedURL, boolean overrideContext,
                                      Object... context)
    {
        String optimizedURL = "optimized:" + expectedURL;
        String encodedURL = "encoded:" + expectedURL;

        Page page = mockPage();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        RequestSecurityManager securityManager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentInvocationMap invocationMap = new NoOpComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Capture<Link> linkCapture = new Capture();
        LinkFactoryListener listener = mockLinkFactoryListener();

        train_getLogicalName(page, logicalName);

        if (!overrideContext)
            train_collectPageActivationContext(collector, page, context);

        train_getBaseURL(securityManager, page, null);
        train_getContextPath(request, "/context");


        train_optimizePath(optimizer, expectedURL, optimizedURL);
        train_encodeURL(response, optimizedURL, encodedURL);

        listener.createdPageRenderLink(capture(linkCapture));

        replay();


        LinkFactory factory = new LinkFactoryImpl(request, response, invocationMap, null, optimizer, null,
                                                  securityManager, contextPathEncoder, collector);

        factory.addListener(listener);

        Object[] passedContext = overrideContext ? context : new Object[0];

        Link link = factory.createPageRenderLink(page, overrideContext, passedContext);

        // Make sure the same link is returned.

        assertSame(linkCapture.getValue(), link);

        assertEquals(link.toURI(), encodedURL);

        verify();
    }

    @Test
    public void simple_component_event_link()
    {

        testEventLinkCreation("/context/order/edit.foo.bar?t:ac=a/b", "order/Edit", "foo.bar", EventConstants.ACTION,
                              false);
    }

    @Test
    public void component_event_link_with_context()
    {
        testEventLinkCreation("/context/order/edit.foo.bar/fred/barney?t:ac=a/b", "order/Edit", "foo.bar",
                              EventConstants.ACTION,
                              false, "fred", "barney");
    }

    @Test
    public void component_event_link_for_form()
    {
        Link link = testEventLinkCreation("/context/order/edit.foo.bar/fred/barney", "order/Edit", "foo.bar",
                                          EventConstants.ACTION,
                                          true, "fred", "barney");

        assertEquals(link.getParameterValue(InternalConstants.PAGE_CONTEXT_NAME), "a/b");
    }

    @Test
    public void component_event_from_other_page()
    {
        String logicalName = "blocks/AppDisplay";

        String expectedURL = "/base/context/order/view.gnip.gnop:myevent/3/5/9";

        Page primaryPage = mockPage();
        Page activePage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        RequestSecurityManager securityManager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentInvocationMap invocationMap = new NoOpComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();

        String encodedURL = "encoded:" + expectedURL;

        train_getRenderingPage(queue, activePage);

        train_getLogicalName(primaryPage, logicalName);
        train_getLogicalName(activePage, "order/View");

        train_collectPageActivationContext(collector, activePage, "x", "y");

        train_getBaseURL(securityManager, activePage, "/base");

        train_getContextPath(request, "/context");

        train_encodeURL(response, expectedURL, encodedURL);


        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, invocationMap, null, optimizer, queue,
                                                  securityManager, contextPathEncoder, collector);

        Link link = factory.createComponentEventLink(primaryPage, "gnip.gnop", "myevent", true, 3, 5, 9);

        assertEquals(link.toURI(), encodedURL);

        verify();

        assertEquals(link.getParameterValue(InternalConstants.CONTAINER_PAGE_NAME), "blocks/appdisplay");
        assertEquals(link.getParameterValue(InternalConstants.PAGE_CONTEXT_NAME), "x/y");
    }

    private Link testEventLinkCreation(String expectedURL, String logicalName, String nestedId, String eventType,
                                       boolean forForm, Object... context)
    {
        Page primaryPage = mockPage();
        Page activePage = primaryPage;
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        RequestSecurityManager securityManager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentInvocationMap invocationMap = new NoOpComponentInvocationMap();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Capture<Link> linkCapture = new Capture();
        LinkFactoryListener listener = mockLinkFactoryListener();

        String optimizedURL = "optimized:" + expectedURL;
        String encodedURL = "encoded:" + expectedURL;

        train_getRenderingPage(queue, null);

        train_getLogicalName(primaryPage, logicalName);

        train_collectPageActivationContext(collector, primaryPage, "a", "b");

        train_getBaseURL(securityManager, activePage, null);

        train_getContextPath(request, "/context");

        train_optimizePath(optimizer, expectedURL, optimizedURL);
        train_encodeURL(response, optimizedURL, encodedURL);

        listener.createdComponentEventLink(capture(linkCapture));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, invocationMap, null, optimizer, queue,
                                                  securityManager, contextPathEncoder, collector);

        factory.addListener(listener);

        Link link = factory.createComponentEventLink(primaryPage, nestedId, eventType, forForm, context);

        // Make sure the same link is returned.

        assertSame(linkCapture.getValue(), link);

        assertEquals(link.toURI(), encodedURL);

        verify();

        return link;
    }

    protected final void train_collectPageActivationContext(PageActivationContextCollector collector, Page page,
                                                            Object... context)
    {
        expect(collector.collectPageActivationContext(page)).andReturn(context);
    }

    protected final PageActivationContextCollector mockPageActivationContextCollector()
    {
        return newMock(PageActivationContextCollector.class);
    }
}
