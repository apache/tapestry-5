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
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkSourceImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

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
        String pageName = "order/Edit";

        PageActivationContextCollector collector = mockPageActivationContextCollector();
        ComponentEventLinkEncoder linkEncoder = mockComponentEventLinkEncoder();
        Link link = mockLink();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_canonicalizePageName(resolver, pageName, pageName);

        train_collectPageActivationContext(collector, pageName, 3);

        EventContext pageActivationContext = new ArrayEventContext(typeCoercer, 3);
        PageRenderRequestParameters parameters = new PageRenderRequestParameters(pageName, pageActivationContext);

        expect(linkEncoder.createPageRenderLink(parameters)).andReturn(link);

        replay();


        LinkSource source = new LinkSourceImpl(null,
                                               collector, typeCoercer, resolver,
                                               linkEncoder);


        Link actual = source.createPageRenderLink(pageName, false);

        // Make sure the same link is returned.

        assertEquals(actual, link);

        verify();
    }

    private void testPageLinkCreation(String pageName, boolean overrideContext,
                                      Object... context)
    {
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        ComponentEventLinkEncoder linkEncoder = mockComponentEventLinkEncoder();
        Link link = mockLink();
        ComponentClassResolver resolver = mockComponentClassResolver();
        String canonical = "CanonicalPageName";

        train_canonicalizePageName(resolver, pageName, canonical);

        if (!overrideContext)
            train_collectPageActivationContext(collector, canonical, context);

        PageRenderRequestParameters parameters =
                new PageRenderRequestParameters(canonical,
                                                new ArrayEventContext(typeCoercer, context));

        expect(linkEncoder.createPageRenderLink(parameters)).andReturn(link);

        listener.createdPageRenderLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(null,
                                               collector, typeCoercer, resolver,
                                               linkEncoder);

        source.getLinkCreationHub().addListener(listener);

        Object[] passedContext = overrideContext ? context : new Object[0];

        Link returnedLink = source.createPageRenderLink(pageName, overrideContext, passedContext);

        assertSame(returnedLink, link);

        verify();
    }

    protected final ComponentEventLinkEncoder mockComponentEventLinkEncoder()
    {
        return newMock(ComponentEventLinkEncoder.class);
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
    }

    @Test
    public void component_event_from_other_page()
    {
        String primaryPageName = "blocks/AppDisplay";

        Page primaryPage = mockPage();
        Page activePage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        ComponentEventLinkEncoder linkEncoder = mockComponentEventLinkEncoder();
        Link link = mockLink();

        train_getRenderingPage(queue, activePage);

        train_getName(activePage, "order/View");
        train_getName(primaryPage, primaryPageName);

        train_collectPageActivationContext(collector, "order/View", "x", "y");

        EventContext pageActivationContext = new ArrayEventContext(typeCoercer, "x", "y");
        EventContext eventContext = new ArrayEventContext(typeCoercer, 3, 5, 9);

        ComponentEventRequestParameters parameters = new ComponentEventRequestParameters("order/View", primaryPageName,
                                                                                         "gnip.gnop", "myevent",
                                                                                         pageActivationContext,
                                                                                         eventContext);

        expect(linkEncoder.createComponentEventLink(parameters, true)).andReturn(link);

        replay();

        LinkSource source = new LinkSourceImpl(queue,
                                               collector, typeCoercer, null,
                                               linkEncoder);

        assertSame(source.createComponentEventLink(primaryPage, "gnip.gnop", "myevent", true, 3, 5, 9), link);

        verify();
    }

    private void testEventLinkCreation(String pageName,
                                       String nestedId,
                                       String eventType,
                                       boolean forForm,
                                       Object... context)
    {
        Page primaryPage = mockPage();
        PageRenderQueue queue = mockPageRenderQueue();
        PageActivationContextCollector collector = mockPageActivationContextCollector();
        LinkCreationListener listener = mockLinkCreationListener();
        ComponentEventLinkEncoder linkEncoder = mockComponentEventLinkEncoder();
        Link link = mockLink();


        ArrayEventContext eventContext = new ArrayEventContext(typeCoercer, context);

        ArrayEventContext pageEventContext = new ArrayEventContext(
                typeCoercer, "a", "b");

        train_getRenderingPage(queue, null);

        train_getName(primaryPage, pageName);

        train_collectPageActivationContext(collector, pageName, "a", "b");


        ComponentEventRequestParameters parameters =
                new ComponentEventRequestParameters(pageName, pageName,
                                                    nestedId, eventType,
                                                    pageEventContext, eventContext);

        expect(linkEncoder.createComponentEventLink(parameters, forForm)).andReturn(link);

        listener.createdComponentEventLink(link);

        replay();

        LinkSource source = new LinkSourceImpl(queue,
                                               collector, typeCoercer, null,
                                               linkEncoder);

        source.getLinkCreationHub().addListener(listener);

        Link returnedLink = source.createComponentEventLink(primaryPage, nestedId, eventType, forForm, context);

        // Make sure the same link is returned.

        assertSame(returnedLink, link);

        verify();
    }

    protected final void train_collectPageActivationContext(PageActivationContextCollector collector, String pageName,
                                                            Object... context)
    {
        expect(collector.collectPageActivationContext(pageName)).andReturn(context);
    }

    protected final PageActivationContextCollector mockPageActivationContextCollector()
    {
        return newMock(PageActivationContextCollector.class);
    }
}
