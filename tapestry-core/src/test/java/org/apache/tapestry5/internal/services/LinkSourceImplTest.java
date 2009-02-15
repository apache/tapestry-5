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
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.LinkCreationListener;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkSourceImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    private final EventContext EMPTY = new EmptyEventContext();

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }

    @Test
    public void create_page_render_link()
    {
        testPageLinkCreation("order/Edit", false);
    }

    @Test
    public void create_page_render_link_for_index_page()
    {
        testPageLinkCreation("order/Index", false);
    }

    @Test
    public void create_page_render_link_for_index_page_with_context()
    {
        testPageLinkCreation("order/Index", false, 99);
    }

    @Test
    public void create_page_render_link_to_root_index_page()
    {
        testPageLinkCreation("Index", false);
    }

    @Test
    public void create_page_render_link_to_root_index_page_with_context()
    {
        testPageLinkCreation("Index", false, 202);
    }


    @Test
    public void create_page_render_link_with_override_event_context()
    {
        testPageLinkCreation("order/Edit", true, 1, 2);
    }

    @Test
    public void create_page_render_link_with_event_context_from_passivate()
    {
        testPageLinkCreation("order/Edit", false, "from", "passivate");
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

        train_getName(page, logicalName);

        train_collectPageActivationContext(collector, page, 3);

        EventContext pageActivationContext = new ArrayEventContext(typeCoercer, 3);
        PageRenderRequestParameters parameters = new PageRenderRequestParameters(logicalName, pageActivationContext);

        expect(factory.createPageRenderLink(parameters)).andReturn(link);

        replay();


        LinkSource source = new LinkSourceImpl(pageCache, null,
                                               collector, factory, typeCoercer);


        Link actual = source.createPageRenderLink(logicalName, false);

        // Make sure the same link is returned.

        assertEquals(actual, link);

        verify();
    }

    private void testPageLinkCreation(String pageName, boolean overrideContext,
                                      Object... context)
    {
        Page page = mockPage();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        LinkFactory factory = mockLinkFactory();
        Link link = mockLink();

        train_getName(page, pageName);

        if (!overrideContext)
            train_collectPageActivationContext(collector, page, context);

        PageRenderRequestParameters parameters =
                new PageRenderRequestParameters(pageName,
                                                new ArrayEventContext(typeCoercer, context));

        expect(factory.createPageRenderLink(parameters)).andReturn(link);

        listener.createdPageRenderLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(null, null,
                                               collector, factory, typeCoercer);

        source.getLinkCreationHub().addListener(listener);

        Object[] passedContext = overrideContext ? context : new Object[0];

        Link returnedLink = source.createPageRenderLink(page, overrideContext, passedContext);

        assertSame(returnedLink, link);

        verify();
    }

    protected final LinkFactory mockLinkFactory()
    {
        return newMock(LinkFactory.class);
    }

    @Test
    public void simple_component_event_link()
    {

        testEventLinkCreation("order/Edit", "foo.bar", EventConstants.ACTION,
                              false);
    }

    @Test
    public void component_event_link_with_context()
    {
        testEventLinkCreation("order/Edit", "foo.bar",
                              EventConstants.ACTION,
                              false, "fred", "barney");
    }

    @Test
    public void component_event_link_for_form()
    {
        testEventLinkCreation("order/Edit", "foo.bar",
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

        train_getName(primaryPage, logicalName);
        train_getName(activePage, "order/View");

        train_collectPageActivationContext(collector, activePage, "x", "y");

        EventContext pageActivationContext = new ArrayEventContext(typeCoercer, "x", "y");
        EventContext eventContext = new ArrayEventContext(typeCoercer, 3, 5, 9);

        ComponentEventRequestParameters parameters = new ComponentEventRequestParameters("order/View", logicalName,
                                                                                         "gnip.gnop", "myevent",
                                                                                         pageActivationContext,
                                                                                         eventContext);

        expect(factory.createComponentEventLink(parameters, true)).andReturn(link);

        replay();

        LinkSource source = new LinkSourceImpl(null, queue,
                                               collector, factory, typeCoercer);

        assertSame(source.createComponentEventLink(primaryPage, "gnip.gnop", "myevent", true, 3, 5, 9), link);

        verify();
    }

    private void testEventLinkCreation(String logicalName, String nestedId,
                                       String eventType,
                                       boolean forForm, Object... context)
    {
        Page primaryPage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        LinkFactory factory = mockLinkFactory();
        Link link = mockLink();


        ArrayEventContext eventContext = new ArrayEventContext(typeCoercer, context);

        ArrayEventContext pageEventContext = new ArrayEventContext(
                typeCoercer, "a", "b");

        train_getRenderingPage(queue, null);

        train_getName(primaryPage, logicalName);

        train_collectPageActivationContext(collector, primaryPage, "a", "b");


        ComponentEventRequestParameters parameters =
                new ComponentEventRequestParameters(logicalName, logicalName,
                                                    nestedId, eventType,
                                                    pageEventContext, eventContext);

        expect(factory.createComponentEventLink(parameters, forForm)).andReturn(link);

        listener.createdComponentEventLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(null, queue,
                                               collector, factory, typeCoercer);

        source.getLinkCreationHub().addListener(listener);

        Link returnedLink = source.createComponentEventLink(primaryPage, nestedId, eventType, forForm, context);

        // Make sure the same link is returned.

        assertSame(returnedLink, link);

        verify();
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
