// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.linktrans.services;

import java.util.Locale;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.services.ArrayEventContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.linktransform.ComponentEventLinkTransformer;

public class AppComponentEventLinkTransformer implements ComponentEventLinkTransformer
{
    @Inject
    private PersistentLocale persistentLocale;

    @Inject
    private TypeCoercer typeCoercer;

    @Inject
    private LocalizationSetter localizationSetter;

    public ComponentEventRequestParameters decodeComponentEventRequest(Request request)
    {
        if (!request.getPath().equals("/event"))
            return null;

        String localeName = request.getParameter("x:locale");
        String pageName = request.getParameter("x:page");
        String container = request.getParameter("x:container");
        String id = request.getParameter("x:id");
        String pac = request.getParameter("x:pac");
        String ec = request.getParameter("x:ec");
        String type = request.getParameter("x:type");

        if (localeName != null)
            localizationSetter.setLocaleFromLocaleName(localeName);

        return new ComponentEventRequestParameters(pageName, container == null ? pageName : container, id, type,
                toContext(pac), toContext(ec));
    }

    private EventContext toContext(String value)
    {
        if (value == null)
            return new EmptyEventContext();

        return new ArrayEventContext(typeCoercer, (Object[]) value.split("/"));
    }

    public Link transformComponentEventLink(Link defaultLink, ComponentEventRequestParameters parameters)
    {
        Link link = defaultLink.copyWithBasePath("/event");

        for (String name : defaultLink.getParameterNames())
        {
            link.removeParameter(name);
        }

        Locale locale = persistentLocale.get();

        if (locale != null)
        {
            link.addParameter("x:locale", locale.toString());
        }

        link.addParameter("x:type", parameters.getEventType());

        addEventContext(link, "x:ec", parameters.getEventContext());
        addEventContext(link, "x:pac", parameters.getPageActivationContext());

        link.addParameter("x:page", parameters.getActivePageName());
        link.addParameter("x:id", parameters.getNestedComponentId());

        if (!parameters.getActivePageName().equals(parameters.getContainingPageName()))
            link.addParameter("x:container", parameters.getContainingPageName());

        return link;

    }

    private void addEventContext(Link link, String parameterName, EventContext eventContext)
    {
        int count = eventContext.getCount();

        if (count == 0)
            return;

        StringBuilder builder = new StringBuilder();
        String sep = "";

        for (int i = 0; i < count; i++)
        {
            builder.append(sep);
            builder.append(eventContext.get(String.class, i));
            sep = "/";
        }

        link.addParameter(parameterName, builder.toString());
    }
}
