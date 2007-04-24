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

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.Defense;

/**
 * Handles a PageLink as specified by a PageLinkPathSource by activating and then rendering the
 * page.
 */
public class PageLinkHandlerImpl implements PageLinkHandler
{
    private final RequestPageCache _cache;

    public PageLinkHandlerImpl(RequestPageCache cache)
    {
        _cache = cache;
    }

    public void handle(String logicalPageName, Object[] context, PageRenderer renderer)
    {
        PageLinkTarget target = new PageLinkTarget(logicalPageName);
        ComponentInvocation invocation = new ComponentInvocation(target, context);

        handle(invocation, renderer);
    }

    public void handle(ComponentInvocation invocation, PageRenderer renderer)
    {
        InvocationTarget target = invocation.getTarget();
        PageLinkTarget pageLinkTarget = Defense.cast(target, PageLinkTarget.class, "target");
        Page page = _cache.get(pageLinkTarget.getPageName());
        // Fire a notification so that the page can set itself up for the given context

        page.getRootElement().triggerEvent(
                TapestryConstants.ACTIVATE_EVENT,
                invocation.getContext(),
                null);

        renderer.renderPage(page);

    }
}
