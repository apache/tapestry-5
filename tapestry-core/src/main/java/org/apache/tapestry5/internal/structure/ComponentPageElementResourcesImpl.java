// Copyright 2008-2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.internal.services.LinkSource;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

import java.io.IOException;

public class ComponentPageElementResourcesImpl implements ComponentPageElementResources
{
    private final ComponentResourceSelector selector;

    private final ComponentMessagesSource componentMessagesSource;

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    private final ContextValueEncoder contextValueEncoder;

    private final LinkSource linkSource;

    private final RequestPageCache requestPageCache;

    private final ComponentClassResolver componentClassResolver;

    private final LoggerSource loggerSource;

    private final OperationTracker tracker;

    private final PerthreadManager perThreadManager;

    private final boolean productionMode, componentTracingEnabled;

    private final RequestGlobals requestGlobals;

    public ComponentPageElementResourcesImpl(ComponentResourceSelector selector,
                                             ComponentMessagesSource componentMessagesSource, TypeCoercer typeCoercer,
                                             ComponentClassCache componentClassCache, ContextValueEncoder contextValueEncoder, LinkSource linkSource,
                                             RequestPageCache requestPageCache, ComponentClassResolver componentClassResolver,
                                             LoggerSource loggerSource, OperationTracker tracker, PerthreadManager perThreadManager, boolean productionMode, boolean componentTracingEnabled, RequestGlobals requestGlobals)
    {
        this.selector = selector;
        this.componentMessagesSource = componentMessagesSource;
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
        this.contextValueEncoder = contextValueEncoder;
        this.linkSource = linkSource;
        this.requestPageCache = requestPageCache;
        this.componentClassResolver = componentClassResolver;
        this.loggerSource = loggerSource;
        this.tracker = tracker;
        this.perThreadManager = perThreadManager;
        this.productionMode = productionMode;
        this.componentTracingEnabled = componentTracingEnabled;
        this.requestGlobals = requestGlobals;
    }

    public ComponentResourceSelector getSelector()
    {
        return selector;
    }

    public Messages getMessages(ComponentModel componentModel)
    {
        return componentMessagesSource.getMessages(componentModel, selector);
    }

    public <S, T> T coerce(S input, Class<T> targetType)
    {
        return typeCoercer.coerce(input, targetType);
    }

    public Class toClass(String className)
    {
        return componentClassCache.forName(className);
    }

    public Link createComponentEventLink(ComponentResources resources, String eventType, boolean forForm,
                                         Object... context)
    {
        Page page = requestPageCache.get(resources.getPageName());

        return linkSource.createComponentEventLink(page, resources.getNestedId(), eventType, forForm,
                defaulted(context));
    }

    public Link createPageRenderLink(String pageName, boolean override, Object... context)
    {
        return linkSource.createPageRenderLink(pageName, override, defaulted(context));
    }

    public Link createPageRenderLink(Class pageClass, boolean override, Object... context)
    {
        assert pageClass != null;
        String pageName = componentClassResolver.resolvePageClassNameToPageName(pageClass.getName());

        return linkSource.createPageRenderLink(pageName, override, defaulted(context));
    }

    public Logger getEventLogger(Logger componentLogger)
    {
        String name = "tapestry.events." + componentLogger.getName();

        return loggerSource.getLogger(name);
    }

    public String toClient(Object value)
    {
        return contextValueEncoder.toClient(value);
    }

    public <T> T toValue(Class<T> requiredType, String clientValue)
    {
        return contextValueEncoder.toValue(requiredType, clientValue);
    }

    private Object[] defaulted(Object[] context)
    {
        return context == null ? CommonsUtils.EMPTY_STRING_ARRAY : context;
    }

    public <T> T invoke(String description, Invokable<T> operation)
    {
        return tracker.invoke(description, operation);
    }

    public <T> T perform(String description, IOOperation<T> operation) throws IOException
    {
        return tracker.perform(description, operation);
    }

    public void run(String description, Runnable operation)
    {
        tracker.run(description, operation);
    }

    public <T> PerThreadValue<T> createPerThreadValue()
    {
        return perThreadManager.createValue();
    }

    public boolean isRenderTracingEnabled()
    {
        if (productionMode)
        {
            return false;
        }

        if (componentTracingEnabled)
        {
            return true;
        }

        Request request = requestGlobals.getRequest();

        if (request == null)
        {
            return false;
        }

        return "true".equals(request.getParameter("t:component-trace"));
    }

}
