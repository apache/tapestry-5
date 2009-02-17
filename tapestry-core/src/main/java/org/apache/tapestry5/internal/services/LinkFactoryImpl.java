// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.*;

import java.util.Locale;

public class LinkFactoryImpl implements LinkFactory
{
    private final Request request;

    private final Response response;

    private final RequestSecurityManager requestSecurityManager;

    private final RequestPathOptimizer optimizer;

    private final PersistentLocale persistentLocale;

    private final ContextValueEncoder valueEncoder;

    private final URLEncoder urlEncoder;

    private static final int BUFFER_SIZE = 100;

    private static final char SLASH = '/';

    public LinkFactoryImpl(Request request, Response response, RequestSecurityManager requestSecurityManager,
                           RequestPathOptimizer optimizer, PersistentLocale persistentLocale,
                           ContextValueEncoder valueEncoder, URLEncoder urlEncoder)
    {
        this.request = request;
        this.response = response;
        this.requestSecurityManager = requestSecurityManager;
        this.optimizer = optimizer;
        this.persistentLocale = persistentLocale;
        this.valueEncoder = valueEncoder;
        this.urlEncoder = urlEncoder;
    }

    public Link createComponentEventLink(ComponentEventRequestParameters parameters, boolean forForm)
    {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        // Build up the absolute URI.

        String activePageName = parameters.getActivePageName();
        String containingPageName = parameters.getContainingPageName();
        String eventType = parameters.getEventType();

        String nestedComponentId = parameters.getNestedComponentId();
        boolean hasComponentId = InternalUtils.isNonBlank(nestedComponentId);

        String baseURL = requestSecurityManager.getBaseURL(activePageName);

        if (baseURL != null)
            builder.append(baseURL);

        builder.append(request.getContextPath());

        Locale locale = persistentLocale.get();

        if (locale != null)
        {
            builder.append(SLASH);
            builder.append(locale.toString());
        }

        builder.append(SLASH);
        builder.append(activePageName.toLowerCase());

        if (hasComponentId)
        {
            builder.append('.');
            builder.append(nestedComponentId);
        }

        if (!hasComponentId || !eventType.equals(EventConstants.ACTION))
        {
            builder.append(":");
            builder.append(eventType.toLowerCase());
        }

        appendContext(parameters.getEventContext(), builder);

        Link result = new LinkImpl(builder.toString(), baseURL == null, forForm, response, optimizer);

        EventContext pageActivationContext = parameters.getPageActivationContext();

        if (pageActivationContext.getCount() != 0)
        {
            // Reuse the builder            
            builder.setLength(0);
            appendContext(pageActivationContext, builder);

            // Omit that first slash
            result.addParameter(InternalConstants.PAGE_CONTEXT_NAME, builder.substring(1));
        }

        // TAPESTRY-2044: Sometimes the active page drags in components from another page and we
        // need to differentiate that.

        if (!containingPageName.equalsIgnoreCase(activePageName))
            result.addParameter(InternalConstants.CONTAINER_PAGE_NAME, containingPageName.toLowerCase());

        return result;
    }


    public Link createPageRenderLink(PageRenderRequestParameters parameters)
    {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        // Build up the absolute URI.

        String activePageName = parameters.getLogicalPageName();

        String baseURL = requestSecurityManager.getBaseURL(activePageName);

        if (baseURL != null)
            builder.append(baseURL);

        builder.append(request.getContextPath());

        Locale locale = persistentLocale.get();

        if (locale != null)
        {
            builder.append(SLASH);
            builder.append(locale.toString());
        }

        builder.append(SLASH);
        builder.append(activePageName.toLowerCase());

        appendContext(parameters.getActivationContext(), builder);

        return new LinkImpl(builder.toString(), baseURL == null, false, response, optimizer);
    }

    public void appendContext(EventContext context, StringBuilder builder)
    {
        for (int i = 0; i < context.getCount(); i++)
        {
            Object raw = context.get(Object.class, i);

            String valueEncoded = raw == null ? null : valueEncoder.toClient(raw);
            String urlEncoded = urlEncoder.encode(valueEncoded);

            builder.append(SLASH);

            builder.append(urlEncoded);
        }
    }

    private String trimIndex(String pageName)
    {
        int lastSlash = pageName.lastIndexOf('/');

        String name = lastSlash > 0
                      ? pageName.substring(lastSlash + 1)
                      : pageName;

        if (name.equalsIgnoreCase("index"))
            return lastSlash > 0
                   ? pageName.substring(0, lastSlash)
                   : "";

        return pageName;
    }
}
