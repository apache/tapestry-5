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

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;

import java.util.Map;

public class RequestPageCacheImpl implements RequestPageCache, ThreadCleanupListener
{
    private final PagePool pagePool;

    /**
     * Keyed on logical page name (case insensitive).
     */
    private final Map<String, Page> cache = CollectionFactory.newCaseInsensitiveMap();

    public RequestPageCacheImpl(PagePool pagePool)
    {
        this.pagePool = pagePool;
    }

    public Page get(String logicalPageName)
    {
        Page page = cache.get(logicalPageName);

        if (page == null)
        {
            page = pagePool.checkout(logicalPageName);

            page.attached();

            cache.put(logicalPageName, page);
        }

        return page;
    }

    /**
     * At the end of the request, when the thread cleanup event occurs, release any pages attached to the request back
     * to the page pool.
     */
    public void threadDidCleanup()
    {
        for (Page p : cache.values())
            pagePool.release(p);
    }
}
