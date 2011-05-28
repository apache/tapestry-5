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

import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.InvalidationListener;

public class PageSourceImpl implements PageSource, InvalidationListener
{
    private final PageLoader pageLoader;

    private static final class CachedPageKey
    {
        final String pageName;

        final Locale locale;

        public CachedPageKey(String pageName, Locale locale)
        {
            this.pageName = pageName;
            this.locale = locale;
        }

        public int hashCode()
        {
            return 37 * pageName.hashCode() + locale.hashCode();
        }

        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (!(obj instanceof CachedPageKey))
                return false;

            CachedPageKey other = (CachedPageKey) obj;

            return pageName.equals(other.pageName) && locale.equals(other.locale);
        }
    }

    private final Map<CachedPageKey, Page> pageCache = CollectionFactory.newConcurrentMap();

    public PageSourceImpl(PageLoader pageLoader)
    {
        this.pageLoader = pageLoader;
    }

    public synchronized void objectWasInvalidated()
    {
        pageCache.clear();
    }

    public Page getPage(String canonicalPageName, Locale locale)
    {
        CachedPageKey key = new CachedPageKey(canonicalPageName, locale);

        if (!pageCache.containsKey(key))
        {
            // In rare race conditions, we may see the same page loaded multiple times across
            // different threads. The last built one will "evict" the others from the page cache,
            // and the earlier ones will be GCed.

            Page page = pageLoader.loadPage(canonicalPageName, locale);

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
