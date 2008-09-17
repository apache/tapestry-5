// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.internal.services.ComponentMessagesSource;
import org.apache.tapestry5.internal.services.LinkFactory;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ContextValueEncoder;

import java.util.Locale;

public class ComponentPageElementResourcesImpl implements ComponentPageElementResources
{
    private final Locale locale;

    private final ComponentMessagesSource componentMessagesSource;

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    private final ContextValueEncoder contextValueEncoder;

    private final LinkFactory linkFactory;

    private final RequestPageCache requestPageCache;

    private final ComponentClassResolver componentClassResolver;

    public ComponentPageElementResourcesImpl(Locale locale, ComponentMessagesSource componentMessagesSource,
                                             TypeCoercer typeCoercer,
                                             ComponentClassCache componentClassCache,
                                             ContextValueEncoder contextValueEncoder, LinkFactory linkFactory,
                                             RequestPageCache requestPageCache,
                                             ComponentClassResolver componentClassResolver)
    {
        this.componentMessagesSource = componentMessagesSource;
        this.locale = locale;
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
        this.contextValueEncoder = contextValueEncoder;
        this.linkFactory = linkFactory;
        this.requestPageCache = requestPageCache;
        this.componentClassResolver = componentClassResolver;
    }

    public Messages getMessages(ComponentModel componentModel)
    {
        return componentMessagesSource.getMessages(componentModel, locale);
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

        return linkFactory.createComponentEventLink(page, resources.getNestedId(), eventType, forForm, context);
    }

    public Link createPageRenderLink(String pageName, boolean override, Object... context)
    {
        return linkFactory.createPageRenderLink(pageName, override, context);
    }

    public Link createPageRenderLink(Class pageClass, boolean override, Object... context)
    {
        Defense.notNull(pageClass, "pageClass");

        String pageName = componentClassResolver.resolvePageClassNameToPageName(pageClass.getName());

        return linkFactory.createPageRenderLink(pageName, override, context);
    }

    public String toClient(Object value)
    {
        return contextValueEncoder.toClient(value);
    }

    public <T> T toValue(Class<T> requiredType, String clientValue)
    {
        return contextValueEncoder.toValue(requiredType, clientValue);
    }
}
