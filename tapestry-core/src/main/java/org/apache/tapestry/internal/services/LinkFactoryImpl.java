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
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.*;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.ContextValueEncoder;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LinkFactoryImpl implements LinkFactory
{
    private final Request _request;

    private final Response _response;

    private final ComponentInvocationMap _componentInvocationMap;

    private final RequestPageCache _pageCache;

    private final ContextValueEncoder _contextValueEncoder;

    private final RequestPathOptimizer _optimizer;

    private final PageRenderQueue _pageRenderQueue;

    private final RequestSecurityManager _requestSecurityManager;

    private final List<LinkFactoryListener> _listeners = newThreadSafeList();

    private final StrategyRegistry<PassivateContextHandler> _registry;


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
        _request = request;
        _response = response;
        _componentInvocationMap = componentInvocationMap;
        _pageCache = pageCache;
        _optimizer = optimizer;
        _pageRenderQueue = pageRenderQueue;
        _contextValueEncoder = contextValueEncoder;
        _requestSecurityManager = requestSecurityManager;

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

        _registry = StrategyRegistry.newInstance(PassivateContextHandler.class, registrations);
    }

    public void addListener(LinkFactoryListener listener)
    {
        _listeners.add(listener);
    }

    public Link createActionLink(Page page, String nestedId, String eventType, boolean forForm, Object... context)
    {
        notNull(page, "page");
        notBlank(eventType, "action");

        String logicalPageName = page.getLogicalName();

        ActionLinkTarget target = new ActionLinkTarget(eventType, logicalPageName, nestedId);

        String[] contextStrings = toContextStrings(context);

        Page activePage = _pageRenderQueue.getRenderingPage();

        String[] activationContext = collectActivationContextForPage(activePage);

        ComponentInvocation invocation = new ComponentInvocationImpl(target, contextStrings, activationContext);

        String baseURL = _requestSecurityManager.getBaseURL(activePage);

        Link link = new LinkImpl(_response, _optimizer, baseURL, _request.getContextPath(), invocation, forForm);

        // TAPESTRY-2044: Sometimes the active page drags in components from another page and we
        // need to differentiate that.

        if (activePage != page)
            link.addParameter(InternalConstants.ACTIVE_PAGE_NAME, activePage.getLogicalName().toLowerCase());

        // Now see if the page has an activation context.

        addActivationContextToLink(link, activationContext, forForm);

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
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

        PageLinkTarget target = new PageLinkTarget(logicalPageName);
        ComponentInvocation invocation = new ComponentInvocationImpl(target, context, null);

        String baseURL = _requestSecurityManager.getBaseURL(page);

        Link link = new LinkImpl(_response, _optimizer, baseURL, _request.getContextPath(), invocation, false);

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
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
                PassivateContextHandler contextHandler = _registry.getByInstance(result);

                contextHandler.handle(result, context);

                return true;
            }
        };

        ComponentPageElement rootElement = page.getRootElement();

        rootElement.triggerEvent(TapestryConstants.PASSIVATE_EVENT, null, callback);

        return toContextStrings(context.toArray());
    }

    private String[] toContextStrings(Object[] context)
    {
        if (context == null) return new String[0];

        String[] result = new String[context.length];

        for (int i = 0; i < context.length; i++)
            result[i] = _contextValueEncoder.toClient(context[i]);

        return result;
    }

    public Link createPageLink(String logicalPageName, boolean override, Object... context)
    {
        // This verifies that the page name is valid.
        Page page = _pageCache.get(logicalPageName);

        return createPageLink(page, override, context);
    }
}
