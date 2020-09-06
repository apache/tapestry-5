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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.LinkCreationListener2;
import org.apache.tapestry5.services.PageRenderRequestParameters;

/**
 * A default {@link LinkCreationListener2} that triggers the {@link EventConstants#DECORATE_COMPONENT_EVENT_LINK} and
 * {@link EventConstants#DECORATE_PAGE_RENDER_LINK} events as links are generated.
 * 
 * @since 5.2.0
 */
public class LinkDecorationListener implements LinkCreationListener2
{
    private final ComponentClassResolver resolver;

    private final ComponentSource componentSource;

    private final ComponentModelSource modelSource;

    public LinkDecorationListener(ComponentClassResolver resolver, ComponentSource componentSource,
            ComponentModelSource modelSource)
    {
        this.resolver = resolver;
        this.componentSource = componentSource;
        this.modelSource = modelSource;
    }

    public void createdComponentEventLink(Link link, ComponentEventRequestParameters parameters)
    {
        trigger(parameters.getActivePageName(), EventConstants.DECORATE_COMPONENT_EVENT_LINK, link, parameters);
    }

    public void createdPageRenderLink(Link link, PageRenderRequestParameters parameters)
    {
        trigger(parameters.getLogicalPageName(), EventConstants.DECORATE_PAGE_RENDER_LINK, link, parameters);
    }

    private void trigger(String pageName, String eventType, Link link, Object parameters)
    {
        String pageClassName = resolver.resolvePageNameToClassName(pageName);

        ComponentModel model = modelSource.getModel(pageClassName);

        if (model.handlesEvent(eventType))
        {
            Component page = componentSource.getPage(pageName);

            page.getComponentResources().triggerEvent(eventType, new Object[]
            { link, parameters }, null);
        }
    }
}
