// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.events.UpdateListener;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.annotations.IntermediateType;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.util.TimeInterval;
import org.apache.tapestry.services.ComponentClassResolver;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;

/**
 * Registered as an invalidation listener with the page loader, the component messages source, and the component
 * template source. Any time any of those notice a change, then the entire page pool is wiped.
 * <p/>
 * The master page pool is, itself, divided into individual sub-pools, one for each combination of
 * <p/>
 * This code is designed to handle high volume sites and deal with request fluctuations.
 * <p/>
 * A <em>soft limit</em> on the number of page instances is enforced. Asking for a page instance when the soft limit has
 * been reached (or exceeded) will result in a delay until a page instance (released from another thread) is available.
 * The delay time is configurable.
 * <p/>
 * A <em>hard limit</em> on the number of page instances is enforced. This number may not be exceeded. Requesting a page
 * instance when at the hard limit will result in a runtime exception.
 * <p/>
 * As an {@link org.apache.tapestry.internal.events.UpdateListener}, the service will reduce the size of each page's
 * pool by eliminating pages that haven't been used recently.
 *
 * @see org.apache.tapestry.internal.services.PagePoolCache
 */
public class PagePoolImpl implements PagePool, InvalidationListener, UpdateListener
{
    private final Logger logger;

    private final PageLoader pageLoader;

    private final ThreadLocale threadLocale;

    private final ComponentClassResolver resolver;

    private final int softLimit;

    private final long softWait;

    private final int hardLimit;

    private final long activeWindow;

    private final Map<PageLocator, PagePoolCache> pool = CollectionFactory.newMap();

    public PagePoolImpl(Logger logger,

                        PageLoader pageLoader,

                        ThreadLocale threadLocale,

                        ComponentClassResolver resolver,

                        @Symbol("tapestry.page-pool.soft-limit")
                        int softLimit,

                        @Symbol("tapestry.page-pool.soft-wait") @IntermediateType(TimeInterval.class)
                        long softWait,

                        @Symbol("tapestry.page-pool.hard-limit")
                        int hardLimit,

                        @Symbol("tapestry.page-pool.active-window") @IntermediateType(TimeInterval.class)
                        long activeWindow)
    {
        this.logger = logger;
        this.pageLoader = pageLoader;
        this.threadLocale = threadLocale;
        this.resolver = resolver;
        this.softLimit = softLimit;
        this.softWait = softWait;
        this.hardLimit = hardLimit;
        this.activeWindow = activeWindow;
    }

    public Page checkout(String logicalPageName)
    {
        String canonicalPageName = resolver.canonicalizePageName(logicalPageName);

        PagePoolCache cache = get(canonicalPageName, threadLocale.getLocale());

        return cache.checkout();
    }

    public void release(Page page)
    {
        PagePoolCache cache = get(page.getLogicalName(), page.getLocale());

        // If the page is not "clean" of any request/client state, it can't go
        // back in the pool.

        if (page.detached())
        {
            logger.error(ServicesMessages.pageIsDirty(page));

            cache.remove(page);

            return;
        }

        cache.release(page);

    }

    private synchronized PagePoolCache get(String pageName, Locale locale)
    {
        PageLocator locator = new PageLocator(pageName, locale);

        PagePoolCache result = pool.get(locator);

        if (result == null)
        {
            // TODO: It might be nice to allow individual pages to override the default limits.

            result = new PagePoolCache(pageName, locale, pageLoader, softLimit, softWait, hardLimit, activeWindow);

            pool.put(locator, result);
        }

        return result;
    }

    /**
     * Any time templates, classes or messages change, we throw out all instances.
     */
    public synchronized void objectWasInvalidated()
    {
        pool.clear();
    }

    public synchronized void checkForUpdates()
    {
        for (PagePoolCache cache : pool.values())
        {
            cache.cleanup();
        }
    }
}
