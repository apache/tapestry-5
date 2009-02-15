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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.LinkCreationHub;
import org.apache.tapestry5.services.LinkCreationListener;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.util.List;

public class LinkSourceImpl implements LinkSource, LinkCreationHub
{
    private final RequestPageCache pageCache;

    private final PageRenderQueue pageRenderQueue;

    private final PageActivationContextCollector contextCollector;

    private final LinkFactory linkFactory;

    private final List<LinkCreationListener> listeners = CollectionFactory.newThreadSafeList();

    private final TypeCoercer typeCoercer;

    public LinkSourceImpl(
            RequestPageCache pageCache,
            PageRenderQueue pageRenderQueue,
            PageActivationContextCollector contextCollector,
            LinkFactory linkFactory, TypeCoercer typeCoercer)
    {
        this.pageCache = pageCache;
        this.pageRenderQueue = pageRenderQueue;
        this.contextCollector = contextCollector;
        this.linkFactory = linkFactory;
        this.typeCoercer = typeCoercer;
    }

    public Link createComponentEventLink(Page page, String nestedId, String eventType, boolean forForm,
                                         Object... eventContext)
    {
        Defense.notNull(page, "page");
        Defense.notBlank(eventType, "action");

        Page activePage = pageRenderQueue.getRenderingPage();

        // See TAPESTRY-2184
        if (activePage == null)
            activePage = page;

        Object[] pageActivationContext = contextCollector.collectPageActivationContext(activePage);

        ComponentEventRequestParameters parameters
                = new ComponentEventRequestParameters(
                activePage.getName(),
                page.getName(),
                toBlank(nestedId),
                eventType,
                new ArrayEventContext(typeCoercer, pageActivationContext),
                new ArrayEventContext(typeCoercer, eventContext));


        Link link = linkFactory.createComponentEventLink(parameters, forForm);

        for (LinkCreationListener listener : listeners)
            listener.createdComponentEventLink(link);

        return link;
    }

    private String toBlank(String input)
    {
        return input == null ? "" : input;
    }

    public Link createPageRenderLink(Page page, boolean override, Object... pageActivationContext)
    {
        Defense.notNull(page, "page");

        String pageName = page.getName();

        // When override is true, we use the activation context even if empty.

        Object[] context = (override || pageActivationContext.length != 0)
                           ? pageActivationContext
                           : contextCollector.collectPageActivationContext(page);

        PageRenderRequestParameters parameters =
                new PageRenderRequestParameters(pageName,
                                                new ArrayEventContext(typeCoercer, context));

        Link link = linkFactory.createPageRenderLink(parameters);

        for (LinkCreationListener listener : listeners)
            listener.createdPageRenderLink(link);

        return link;
    }

    public Link createPageRenderLink(String logicalPageName, boolean override, Object... context)
    {
        // This verifies that the page name is valid.
        Page page = pageCache.get(logicalPageName);

        return createPageRenderLink(page, override, context);
    }

    public LinkCreationHub getLinkCreationHub()
    {
        return this;
    }

    public void addListener(LinkCreationListener listener)
    {
        Defense.notNull(listener, "listener");

        listeners.add(listener);
    }

    public void removeListener(LinkCreationListener listener)
    {
        Defense.notNull(listener, "listener");

        listeners.remove(listener);
    }
}
