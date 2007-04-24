// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentClassResolver;

public class LinkFactoryImpl implements LinkFactory
{
    private final ContextPathSource _contextPathSource;

    private final URLEncoder _encoder;

    private final ComponentClassResolver _componentClassResolver;

    private final ComponentInvocationMap _componentInvocationMap;

    private final RequestPageCache _pageCache;

    private final List<LinkFactoryListener> _listeners = newThreadSafeList();

    private final StrategyRegistry<PassivateContextHandler> _registry;

    private interface PassivateContextHandler<T>
    {
        void handle(T result, List context);
    }

    public LinkFactoryImpl(ContextPathSource contextPathSource, URLEncoder encoder,
            ComponentClassResolver componentClassResolver,
            ComponentInvocationMap componentInvocationMap, RequestPageCache pageCache)
    {
        _contextPathSource = contextPathSource;
        _encoder = encoder;
        _componentClassResolver = componentClassResolver;
        _componentInvocationMap = componentInvocationMap;
        _pageCache = pageCache;

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
                for (Object o : result)
                    context.add(o);
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

    public Link createActionLink(ComponentPageElement component, String action, boolean forForm,
            Object... context)
    {
        Defense.notBlank(action, "action");

        String pageName = component.getContainingPage().getName();

        String logicalPageName = _componentClassResolver.resolvePageClassNameToPageName(pageName);

        ActionLinkTarget target = new ActionLinkTarget(action, logicalPageName, component
                .getNestedId());
        ComponentInvocation invocation = new ComponentInvocation(target, context);

        Link link = new LinkImpl(_encoder, _contextPathSource.getContextPath(), invocation, forForm);

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
            listener.createdActionLink(link);

        // TODO: query parameter for case where active page != component page.

        return link;
    }

    public Link createPageLink(final Page page)
    {
        Defense.notNull(page, "page");

        String pageName = page.getName();
        String logicalPageName = _componentClassResolver.resolvePageClassNameToPageName(pageName);

        final List context = newList();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            @SuppressWarnings("unchecked")
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                PassivateContextHandler contextHandler = _registry.getByInstance(result);

                contextHandler.handle(result, context);

                return true;
            }
        };

        ComponentPageElement rootElement = page.getRootElement();

        rootElement.triggerEvent(TapestryConstants.PASSIVATE_EVENT, null, handler);

        PageLinkTarget target = new PageLinkTarget(logicalPageName);
        ComponentInvocation invocation = new ComponentInvocation(target, context.toArray());

        Link link = new LinkImpl(_encoder, _contextPathSource.getContextPath(), invocation, false);

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
            listener.createdPageLink(link);

        // TODO: query parameter for case where active page != component page.

        return link;
    }

    public Link createPageLink(String pageName)
    {
        // This verifies that the page name is valid.
        Page page = _pageCache.get(pageName);

        return createPageLink(page);
    }
}
