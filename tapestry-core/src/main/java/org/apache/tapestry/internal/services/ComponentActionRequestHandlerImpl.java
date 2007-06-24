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

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentActionRequestHandler;
import org.apache.tapestry.services.ComponentEventResultProcessor;

public class ComponentActionRequestHandlerImpl implements ComponentActionRequestHandler
{
    private final ComponentEventResultProcessor _resultProcessor;

    private final RequestPageCache _cache;

    private final LinkFactory _linkFactory;

    public ComponentActionRequestHandlerImpl(ComponentEventResultProcessor resultProcessor,
            RequestPageCache cache, LinkFactory linkFactory)
    {
        _resultProcessor = resultProcessor;
        _cache = cache;
        _linkFactory = linkFactory;
    }

    public ActionResponseGenerator handle(String logicalPageName, String nestedComponentId,
            String eventType, String[] context, String[] activationContext)
    {
        Page page = _cache.get(logicalPageName);

        // This is the active page, until we know better.

        ComponentPageElement element = page.getComponentElementByNestedId(nestedComponentId);

        final Holder<ActionResponseGenerator> holder = new Holder<ActionResponseGenerator>();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            @SuppressWarnings("unchecked")
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                ActionResponseGenerator generator = _resultProcessor.processComponentEvent(
                        result,
                        component,
                        methodDescription);

                holder.put(generator);

                return true;
            }
        };

        // If activating the page returns a "navigational result", then don't trigger the action
        // on the component.

        page.getRootElement().triggerEvent(
                TapestryConstants.ACTIVATE_EVENT,
                activationContext,
                handler);

        if (holder.hasValue()) return holder.get();

        element.triggerEvent(eventType, context, handler);

        ActionResponseGenerator result = holder.get();

        if (result == null)
        {
            Link link = _linkFactory.createPageLink(page);

            result = new LinkActionResponseGenerator(link);
        }

        return result;
    }
}
