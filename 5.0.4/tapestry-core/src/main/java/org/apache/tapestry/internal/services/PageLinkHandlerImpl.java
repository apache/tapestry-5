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

import static org.apache.tapestry.ioc.internal.util.Defense.cast;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentEventResultProcessor;

/**
 * Handles a PageLink as specified by a PageLinkPathSource by activating and then rendering the
 * page.
 */
public class PageLinkHandlerImpl implements PageLinkHandler
{
    private final RequestPageCache _cache;

    private final ComponentEventResultProcessor _resultProcessor;

    public PageLinkHandlerImpl(RequestPageCache cache, ComponentEventResultProcessor resultProcessor)
    {
        _cache = cache;
        _resultProcessor = resultProcessor;
    }

    public ActionResponseGenerator handle(String logicalPageName, String[] context,
            PageRenderer renderer)
    {
        PageLinkTarget target = new PageLinkTarget(logicalPageName);
        ComponentInvocation invocation = new ComponentInvocation(target,  context, context);

        return handle(invocation, renderer);
    }

    public ActionResponseGenerator handle(ComponentInvocation invocation, PageRenderer renderer)
    {
        InvocationTarget target = invocation.getTarget();
        // I really don't like this cast; there must be a way to get rid of it?
        PageLinkTarget pageLinkTarget = cast(target, PageLinkTarget.class, "target");
        Page page = _cache.get(pageLinkTarget.getPageName());

        // Fire a notification so that the page can set itself up for the given context.

        // This duplicates some code from ActionLinkHandlerImpl.

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

                return true; // abort the event
            }
        };

        page.getRootElement().triggerEvent(
                TapestryConstants.ACTIVATE_EVENT,
                invocation.getContext(),
                handler);

        if (holder.hasValue())
            return holder.get();

        renderer.renderPage(page);

        return null;
    }
}
