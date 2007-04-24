// Copyright 2006, 2007 The Apache Software Foundation
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
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.net.URLCodec;
import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.services.TypeCoercer;
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

    private final TypeCoercer _typeCoercer;

    private final List<LinkFactoryListener> _listeners = newThreadSafeList();

    private final StrategyRegistry<PassivateContextHandler> _registry;

    // URLCodec is thread-safe and sharable.
    private final URLCodec _urlCodec = new URLCodec();

    private interface PassivateContextHandler<T>
    {
        void handle(T result, List context);
    }

    public LinkFactoryImpl(ContextPathSource contextPathSource, URLEncoder encoder,
            ComponentClassResolver componentClassResolver,
            ComponentInvocationMap componentInvocationMap, RequestPageCache pageCache,
            TypeCoercer typeCoercer)
    {
        _contextPathSource = contextPathSource;
        _encoder = encoder;
        _componentClassResolver = componentClassResolver;
        _componentInvocationMap = componentInvocationMap;
        _pageCache = pageCache;
        _typeCoercer = typeCoercer;

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
        notBlank(action, "action");

        Page containingPage = component.getContainingPage();
        String pageName = containingPage.getName();

        String logicalPageName = _componentClassResolver.resolvePageClassNameToPageName(pageName);

        ActionLinkTarget target = new ActionLinkTarget(action, logicalPageName, component
                .getNestedId());

        String[] contextStrings = toContextStrings(context);

        String[] activationContext = collectActivationContextForPage(containingPage);

        ComponentInvocation invocation = new ComponentInvocation(target, contextStrings,
                activationContext);

        Link link = new LinkImpl(_encoder, _contextPathSource.getContextPath(), invocation, forForm);

        // Now see if the page has an activation context.

        addActivationContextToLink(link, activationContext);

        // TODO: query parameter for case where active page != component page.

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
            listener.createdActionLink(link);

        return link;
    }

    private void addActivationContextToLink(Link link, String[] activationContext)
    {
        if (activationContext.length == 0)
            return;

        StringBuilder builder = new StringBuilder();

        try
        {
            for (int i = 0; i < activationContext.length; i++)
            {
                if (i > 0)
                    builder.append("/");

                builder.append(_urlCodec.encode(activationContext[i]));
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        link.addParameter(InternalConstants.PAGE_CONTEXT_NAME, builder.toString());

    }

    public Link createPageLink(final Page page, Object... activationContext)
    {
        notNull(page, "page");

        String pageName = page.getName();
        String logicalPageName = _componentClassResolver.resolvePageClassNameToPageName(pageName);

        String[] context = activationContext.length != 0 ? toContextStrings(activationContext)
                : collectActivationContextForPage(page);

        PageLinkTarget target = new PageLinkTarget(logicalPageName);
        ComponentInvocation invocation = new ComponentInvocation(target, context, null);

        Link link = new LinkImpl(_encoder, _contextPathSource.getContextPath(), invocation, false);

        _componentInvocationMap.store(link, invocation);

        for (LinkFactoryListener listener : _listeners)
            listener.createdPageLink(link);

        return link;
    }

    /**
     * Returns a list of objects acquired by invoking triggering the passivate event on the page's
     * root element. May return an empty list.
     */
    private String[] collectActivationContextForPage(final Page page)
    {
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

        return toContextStrings(context.toArray());
    }

    private String[] toContextStrings(Object[] context)
    {
        if (context == null)
            return new String[0];

        String[] result = new String[context.length];

        for (int i = 0; i < context.length; i++)
            result[i] = _typeCoercer.coerce(context[i], String.class);

        return result;
    }

    public Link createPageLink(String pageName, Object... context)
    {
        // This verifies that the page name is valid.
        Page page = _pageCache.get(pageName);

        return createPageLink(page, context);
    }
}
