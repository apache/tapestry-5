// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.cron.IntervalSchedule;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class PageSourceImpl implements PageSource, InvalidationListener
{
    private final ComponentRequestSelectorAnalyzer selectorAnalyzer;

    private final PageLoader pageLoader;

    private final long activeWindow;

    private final Logger logger;

    private static final class CachedPageKey
    {
        final String pageName;

        final ComponentResourceSelector selector;

        public CachedPageKey(String pageName, ComponentResourceSelector selector)
        {
            this.pageName = pageName;
            this.selector = selector;
        }

        public int hashCode()
        {
            return 37 * pageName.hashCode() + selector.hashCode();
        }

        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (!(obj instanceof CachedPageKey))
                return false;

            CachedPageKey other = (CachedPageKey) obj;

            return pageName.equals(other.pageName) && selector.equals(other.selector);
        }
    }

    private final Map<CachedPageKey, Page> pageCache = CollectionFactory.newConcurrentMap();

    public PageSourceImpl(PageLoader pageLoader, ComponentRequestSelectorAnalyzer selectorAnalyzer,

                          @Symbol(SymbolConstants.PAGE_SOURCE_ACTIVE_WINDOW)
                          @IntermediateType(TimeInterval.class)
                          long activeWindow, Logger logger)
    {
        this.pageLoader = pageLoader;
        this.selectorAnalyzer = selectorAnalyzer;
        this.activeWindow = activeWindow;
        this.logger = logger;
    }

    @PostInjection
    public void startJanitor(PeriodicExecutor executor, @Symbol(SymbolConstants.PAGE_SOURCE_CHECK_INTERVAL)
    @IntermediateType(TimeInterval.class)
    long checkInterval)
    {
        executor.addJob(new IntervalSchedule(checkInterval), "PagePool cleanup", new Runnable()
        {
            public void run()
            {
                prune();
            }
        });
    }

    private void prune()
    {
        Iterator<Page> iterator = pageCache.values().iterator();

        int count = 0;

        long cutoff = System.currentTimeMillis() - activeWindow;

        while (iterator.hasNext())
        {
            Page page = iterator.next();

            if (page.getLastAttachTime() <= cutoff)
            {
                count++;
                iterator.remove();

                logger.info(String.format("Pruned page %s (%s), not accessed since %s.",
                        page.getName(),
                        page.getSelector().toShortString(),
                        new Date(page.getLastAttachTime())));
            }
        }

        if (count > 0)
        {
            logger.info(String.format("Pruned %d page %s.", count, count == 1 ? "instance" : "instances"));
        }
    }

    public synchronized void objectWasInvalidated()
    {
        pageCache.clear();
    }

    public Page getPage(String canonicalPageName)
    {
        ComponentResourceSelector selector = selectorAnalyzer.buildSelectorForRequest();

        CachedPageKey key = new CachedPageKey(canonicalPageName, selector);

        if (!pageCache.containsKey(key))
        {
            // In rare race conditions, we may see the same page loaded multiple times across
            // different threads. The last built one will "evict" the others from the page cache,
            // and the earlier ones will be GCed.

            Page page = pageLoader.loadPage(canonicalPageName, selector);

            pageCache.put(key, page);
        }

        // From good authority (Brian Goetz), this is the best way to ensure that the
        // loaded page, with all of its semi-mutable construction-time state, is
        // properly published.

        return pageCache.get(key);
    }

    public void clearCache()
    {
        pageCache.clear();
    }
}
