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

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.*;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LinkFactoryImpl implements LinkFactory
{
    private final Request request;

    private final Response response;

    private final ComponentInvocationMap componentInvocationMap;

    private final RequestPageCache pageCache;

    private final ContextValueEncoder contextValueEncoder;

    private final RequestPathOptimizer optimizer;

    private final PageRenderQueue pageRenderQueue;

    private final RequestSecurityManager requestSecurityManager;

    private final List<LinkFactoryListener> listeners = newThreadSafeList();

    private final StrategyRegistry<PassivateContextHandler> registry;


    private interface PassivateContextHandler<T>
    {
        void handle(T result, List context);
    }

    public LinkFactoryImpl(Request request,
                           Response response,
                           ComponentInvocationMap componentInvocationMap,
                           RequestPageCache pageCache,
                           RequestPathOptimizer optimizer,
                           PageRenderQueue pageRenderQueue,
                           ContextValueEncoder contextValueEncoder,
                           RequestSecurityManager requestSecurityManager)
    {
        this.request = request;
        this.response = response;
        this.componentInvocationMap = componentInvocationMap;
        this.pageCache = pageCache;
        this.optimizer = optimizer;
        this.pageRenderQueue = pageRenderQueue;
        this.contextValueEncoder = contextValueEncoder;
        this.requestSecurityManager = requestSecurityManager;

        Map<Class, PassivateContextHandler> registrations = newMap();

        registrations.put(Object.class, new PassivateContextHandler()
        {
            @SuppressWarnings("unchecked")
            public void handle(Object result, List context)
            {
                context.add(result);
            }
        });

        registrations.put(Object[].class, new PassivateContextHandler<Object[]>()
        {

            @SuppressWarnings("unchecked")
            public void handle(Object[] result, List context)
            {
                context.addAll(Arrays.asList(result));
            }
        });

        registrations.put(Collection.class, new PassivateContextHandler<Collection>()
        {
            @SuppressWarnings("unchecked")
            public void handle(Collection result, List context)
            {
                context.addAll(result);
            }
        });

        registry = StrategyRegistry.newInstance(PassivateContextHandler.class, registrations);
    }

    public void addListener(LinkFactoryListener listener)
    {
        listeners.add(listener);
    }

    public Link createActionLink(Page page, String nestedId, String eventType, boolean forForm, Object... context)
    {
        notNull(page, "page");
        notBlank(eventType, "action");

        Page activePage = pageRenderQueue.getRenderingPage();

        // See TAPESTRY-2184
        if (activePage == null) activePage = page;

        ActionLinkTarget target = new ActionLinkTarget(eventType, activePage.getLogicalName(), nestedId);

        String[] contextStrings = toContextStrings(context);

        String[] activationContext = collectActivationContextForPage(activePage);

        ComponentInvocation invocation = new ComponentInvocationImpl(target, contextStrings, activationContext);

        String baseURL = requestSecurityManager.getBaseURL(activePage);

        Link link = new LinkImpl(response, optimizer, baseURL, request.getContextPath(), invocation, forForm);

        // TAPESTRY-2044: Sometimes the active page drags in components from another page and we
        // need to differentiate that.

        if (activePage != page)
            link.addParameter(InternalConstants.CONTAINER_PAGE_NAME, page.getLogicalName().toLowerCase());

        // Now see if the page has an activation context.

        addActivationContextToLink(link, activationContext, forForm);

        componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : listeners)
            listener.createdActionLink(link);

        return link;
    }

    private void addActivationContextToLink(Link link, String[] activationContext, boolean forForm)
    {
        if (activationContext.length == 0) return;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < activationContext.length; i++)
        {
            if (i > 0) builder.append("/");

            builder.append(forForm
                           ? TapestryInternalUtils.escapePercentAndSlash(activationContext[i])
                           : TapestryInternalUtils.encodeContext(activationContext[i]));
        }

        link.addParameter(InternalConstants.PAGE_CONTEXT_NAME, builder.toString());
    }

    public Link createPageLink(Page page, boolean override, Object... activationContext)
    {
        notNull(page, "page");

        String logicalPageName = page.getLogicalName();

        // When override is true, we use the activation context even if empty.

        String[] context = (override || activationContext.length != 0) ? toContextStrings(
                activationContext) : collectActivationContextForPage(page);

        // Strip a trailing "/index" from the path.

        int lastSlashx = logicalPageName.lastIndexOf("/");

        String lastTerm = lastSlashx < 0 ? logicalPageName : logicalPageName.substring(lastSlashx + 1);

        // This, alas, duplicates some logic inside ComponentClassResolverImpl ...

        if (lastTerm.equalsIgnoreCase("index"))
        {
            logicalPageName = lastSlashx < 0 ? "" : logicalPageName.substring(0, lastSlashx);
        }

        PageLinkTarget target = new PageLinkTarget(logicalPageName);

        ComponentInvocation invocation = new ComponentInvocationImpl(target, context, null);

        String baseURL = requestSecurityManager.getBaseURL(page);

        Link link = new LinkImpl(response, optimizer, baseURL, request.getContextPath(), invocation, false);

        componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : listeners)
            listener.createdPageLink(link);

        return link;
    }

    /**
     * Returns a list of objects acquired by invoking triggering the passivate event on the page's root element. May
     * return an empty list.
     */
    private String[] collectActivationContextForPage(final Page page)
    {
        final List context = newList();

        ComponentEventCallback callback = new ComponentEventCallback()
        {
            @SuppressWarnings("unchecked")
            public boolean handleResult(Object result)
            {
                PassivateContextHandler contextHandler = registry.getByInstance(result);

                contextHandler.handle(result, context);

                return true;
            }
        };

        ComponentPageElement rootElement = page.getRootElement();

        rootElement.triggerEvent(EventConstants.PASSIVATE, null, callback);

        return toContextStrings(context.toArray());
    }

    private String[] toContextStrings(Object[] context)
    {
        if (context == null) return new String[0];

        String[] result = new String[context.length];

        for (int i = 0; i < context.length; i++)
        {

            Object value = context[i];

            String encoded = value == null ? null : contextValueEncoder.toClient(value);

            if (InternalUtils.isBlank(encoded))
                throw new RuntimeException(ServicesMessages.contextValueMayNotBeNull());

            result[i] = encoded;
        }

        return result;
    }

    public Link createPageLink(String logicalPageName, boolean override, Object... context)
    {
        // This verifies that the page name is valid.
        Page page = pageCache.get(logicalPageName);

        return createPageLink(page, override, context);
    }
}
