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

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentEventResultProcessor;

public class ActionLinkHandlerImpl implements ActionLinkHandler
{
    private final ComponentEventResultProcessor _resultProcessor;

    private final RequestPageCache _cache;

    private final LinkFactory _linkFactory;

    public ActionLinkHandlerImpl(ComponentEventResultProcessor resultProcessor,
            RequestPageCache cache, LinkFactory linkFactory)
    {
        _resultProcessor = resultProcessor;
        _cache = cache;
        _linkFactory = linkFactory;
    }

    public ActionResponseGenerator handle(String logicalPageName, String nestedComponentId,
            String eventType, String[] context)
    {
        ActionLinkTarget actionLinkTarget = new ActionLinkTarget(eventType, logicalPageName,
                nestedComponentId);

        ComponentInvocation invocation = new ComponentInvocation(actionLinkTarget, context);

        return handle(invocation);
    }

    public ActionResponseGenerator handle(ComponentInvocation invocation)
    {
        InvocationTarget target = invocation.getTarget();
        // TODO: Not too happy about needing this cast; can the method be moved up to
        // InvocationTarget or ComponentInvocation?
        ActionLinkTarget actionLinkTarget = Defense.cast(target, ActionLinkTarget.class, "target");
        Page page = _cache.get(actionLinkTarget.getPageName());

        // This is the active page, until we know better.

        ComponentPageElement element = page.getComponentElementByNestedId(actionLinkTarget
                .getComponentNestedId());

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

        element.triggerEvent(actionLinkTarget.getAction(), invocation.getContext(), handler);

        ActionResponseGenerator result = holder.get();

        if (result == null)
        {
            Link link = _linkFactory.createPageLink(page);

            return new LinkActionResponseGenerator(link);
        }

        return result;
    }
}
