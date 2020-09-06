// Copyright 2010-2013 The Apache Software Foundation
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

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.slf4j.Logger;

import java.util.Map;

/**
 * In Tapestry 5.1, the implementation of this worked with the page pool (a pool of page instances, reserved
 * to individual requests/threads). Page pooling was deprecated in 5.2 and removed in 5.3.
 *
 * @since 5.2
 */
@Scope(ScopeConstants.PERTHREAD)
public class RequestPageCacheImpl implements RequestPageCache, Runnable
{
    private final Logger logger;

    private final ComponentClassResolver resolver;

    private final PageSource pageSource;

    private final RequestGlobals requestGlobals;

    private final Map<String, Page> cache = CollectionFactory.newMap();

    public RequestPageCacheImpl(Logger logger, ComponentClassResolver resolver, PageSource pageSource, RequestGlobals requestGlobals)
    {
        this.logger = logger;
        this.resolver = resolver;
        this.pageSource = pageSource;
        this.requestGlobals = requestGlobals;
    }

    @PostInjection
    public void listenForThreadCleanup(PerthreadManager perthreadManager)
    {
        perthreadManager.addThreadCleanupCallback(this);
    }

    public void run()
    {
        for (Page page : cache.values())
        {
            try
            {
                page.detached();
            } catch (Throwable t)
            {
                logger.error("Error detaching page {}: {}", page, ExceptionUtils.toMessage(t), t);
            }
        }
    }

    public Page get(String pageName)
    {
        String canonical = resolver.canonicalizePageName(pageName);

        Page page = cache.get(canonical);

        if (page == null)
        {
            page = pageSource.getPage(canonical);

            try
            {
                page.attached();
            } catch (Throwable t)
            {
                throw new RuntimeException(String.format("Unable to attach page %s: %s", canonical,
                        ExceptionUtils.toMessage(t)), t);
            }

            cache.put(canonical, page);
        }

        // A bit of a hack but whatever.
        if (canonical.equals(requestGlobals.getActivePageName()))
        {
            requestGlobals.getRequest().setAttribute(InternalConstants.ACTIVE_PAGE_LOADED, true);
        }

        return page;
    }
}
