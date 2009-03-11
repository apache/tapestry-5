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
import org.apache.tapestry5.services.*;

import java.util.List;

public class LinkSourceImpl implements LinkSource, LinkCreationHub
{
    private final PageRenderQueue pageRenderQueue;

    private final PageActivationContextCollector contextCollector;

    private final ComponentEventLinkEncoder linkEncoder;

    private final List<LinkCreationListener> listeners = CollectionFactory.newThreadSafeList();

    private final TypeCoercer typeCoercer;

    private final ComponentClassResolver resolver;

    public LinkSourceImpl(PageRenderQueue pageRenderQueue,
                          PageActivationContextCollector contextCollector,
                          TypeCoercer typeCoercer,
                          ComponentClassResolver resolver,
                          ComponentEventLinkEncoder linkEncoder)
    {
        this.pageRenderQueue = pageRenderQueue;
        this.contextCollector = contextCollector;
        this.typeCoercer = typeCoercer;
        this.resolver = resolver;
        this.linkEncoder = linkEncoder;
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

        String activePageName = activePage.getName();

        Object[] pageActivationContext = contextCollector.collectPageActivationContext(activePageName);

        ComponentEventRequestParameters parameters
                = new ComponentEventRequestParameters(
                activePageName,
                page.getName(),
                toBlank(nestedId),
                eventType,
                new ArrayEventContext(typeCoercer, pageActivationContext),
                new ArrayEventContext(typeCoercer, eventContext));


        Link link = linkEncoder.createComponentEventLink(parameters, forForm);

        for (LinkCreationListener listener : listeners)
            listener.createdComponentEventLink(link);

        return link;
    }

    private String toBlank(String input)
    {
        return input == null ? "" : input;
    }

    public Link createPageRenderLink(String pageName, boolean override, Object... pageActivationContext)
    {
        // Resolve the page name to its canonical format (the best version for URLs). This also validates
        // the page name.

        String canonical = resolver.canonicalizePageName(pageName);

        Object[] context = (override || pageActivationContext.length != 0)
                           ? pageActivationContext
                           : contextCollector.collectPageActivationContext(canonical);

        PageRenderRequestParameters parameters =
                new PageRenderRequestParameters(canonical,
                                                new ArrayEventContext(typeCoercer, context));

        Link link = linkEncoder.createPageRenderLink(parameters);

        for (LinkCreationListener listener : listeners)
            listener.createdPageRenderLink(link);

        return link;
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
