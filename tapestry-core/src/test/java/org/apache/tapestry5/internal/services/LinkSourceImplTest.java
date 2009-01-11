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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.LinkCreationListener;
import org.easymock.Capture;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkSourceImplTest extends InternalBaseTestCase
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
        testPageLinkCreation("order/Edit", "order/edit", false);
    }

    @Test
    public void create_page_render_link_for_index_page()
    {
        testPageLinkCreation("order/Index", "order", false);
    }

    @Test
    public void create_page_render_link_for_index_page_with_context()
    {
        testPageLinkCreation("order/Index", "order/99", false, 99);
    }

    @Test
    public void create_page_render_link_to_root_index_page()
    {
        testPageLinkCreation("Index", "", false);
    }

    @Test
    public void create_page_render_link_to_root_index_page_with_context()
    {
        testPageLinkCreation("Index", "202", false, 202);
    }


    @Test
    public void create_page_render_link_with_override_event_context()
    {
        testPageLinkCreation("order/Edit", "order/edit/1/2", true, 1, 2);
    }

    @Test
    public void create_page_render_link_with_event_context_from_passivate()
    {
        testPageLinkCreation("order/Edit", "order/edit/from/passivate", false, "from", "passivate");
    }

    @Test
    public void create_page_render_link_by_name()
    {
        String logicalName = "order/Edit";

        Page page = mockPage();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        RequestPageCache pageCache = mockRequestPageCache();
        LinkFactory factory = mockLinkFactory();
        Link link = mockLink();

        train_get(pageCache, logicalName, page);

        train_getLogicalName(page, logicalName);

        train_collectPageActivationContext(collector, page, 3);

        Capture<ComponentInvocation> holder = train_create(factory, page, link);

        replay();


        LinkSource source = new LinkSourceImpl(pageCache, null,
                                               contextPathEncoder, collector, factory);


        Link actual = source.createPageRenderLink(logicalName, false);

        // Make sure the same link is returned.

        assertEquals(actual, link);

        assertEquals(holder.getValue().buildURI(), "order/edit/3");

        verify();
    }

    private void testPageLinkCreation(String logicalName, String expectedURI, boolean overrideContext,
                                      Object... context)
    {
        Page page = mockPage();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        LinkFactory factory = mockLinkFactory();
        Capture<ComponentInvocation> holder = new Capture<ComponentInvocation>();
        Link link = mockLink();

        train_getLogicalName(page, logicalName);

        if (!overrideContext)
            train_collectPageActivationContext(collector, page, context);

        expect(factory.create(eq(page), capture(holder))).andReturn(link);

        listener.createdPageRenderLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(null, null,
                                               contextPathEncoder, collector, factory);

        source.getLinkCreationHub().addListener(listener);

        Object[] passedContext = overrideContext ? context : new Object[0];

        Link returnedLink = source.createPageRenderLink(page, overrideContext, passedContext);

        assertSame(returnedLink, link);

        assertEquals(holder.getValue().buildURI(), expectedURI);

        verify();
    }

    protected final LinkFactory mockLinkFactory() {return newMock(LinkFactory.class);}

    @Test
    public void simple_component_event_link()
    {

        testEventLinkCreation("order/edit.foo.bar?t:ac=a/b", "order/Edit", "foo.bar", EventConstants.ACTION,
                              false);
    }

    @Test
    public void component_event_link_with_context()
    {
        testEventLinkCreation("order/edit.foo.bar/fred/barney?t:ac=a/b", "order/Edit", "foo.bar",
                              EventConstants.ACTION,
                              false, "fred", "barney");
    }

    @Test
    public void component_event_link_for_form()
    {
        testEventLinkCreation("order/edit.foo.bar/fred/barney", "order/Edit", "foo.bar",
                              EventConstants.ACTION,
                              true, "fred", "barney");

        // assertEquals(link.getParameterValue(InternalConstants.PAGE_CONTEXT_NAME), "a/b");
    }

    @Test
    public void component_event_from_other_page()
    {
        String logicalName = "blocks/AppDisplay";

        Page primaryPage = mockPage();
        Page activePage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkFactory factory = mockLinkFactory();
        Link link = mockLink();


        train_getRenderingPage(queue, activePage);

        train_getLogicalName(primaryPage, logicalName);
        train_getLogicalName(activePage, "order/View");

        train_collectPageActivationContext(collector, activePage, "x", "y");

        Capture<ComponentInvocation> holder = train_create(factory, activePage, link);

        link.addParameter(InternalConstants.CONTAINER_PAGE_NAME, "blocks/appdisplay");

        replay();

        LinkSource source = new LinkSourceImpl(null, queue,
                                               contextPathEncoder, collector, factory);

        assertSame(source.createComponentEventLink(primaryPage, "gnip.gnop", "myevent", true, 3, 5, 9), link);

        verify();

        assertEquals(holder.getValue().buildURI(), "order/view.gnip.gnop:myevent/3/5/9");
    }

    private void testEventLinkCreation(String expectedURI, String logicalName, String nestedId,
                                       String eventType,
                                       boolean forForm, Object... context)
    {
        Page primaryPage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        LinkFactory factory = mockLinkFactory();
        Link link = mockLink();

        train_getRenderingPage(queue, null);

        train_getLogicalName(primaryPage, logicalName);

        train_collectPageActivationContext(collector, primaryPage, "a", "b");

        Capture<ComponentInvocation> holder = train_create(factory, primaryPage, link);

        listener.createdComponentEventLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(null, queue,
                                               contextPathEncoder, collector, factory);

        source.getLinkCreationHub().addListener(listener);

        Link returnedLink = source.createComponentEventLink(primaryPage, nestedId, eventType, forForm, context);

        // Make sure the same link is returned.

        assertSame(returnedLink, link);

        verify();

        assertEquals(holder.getValue().buildURI(), expectedURI);
    }

    protected final Capture<ComponentInvocation> train_create(LinkFactory factory, Page primaryPage, Link link)
    {
        Capture<ComponentInvocation> holder = new Capture<ComponentInvocation>();
        expect(factory.create(eq(primaryPage), capture(holder))).andReturn(link);
        return holder;
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
