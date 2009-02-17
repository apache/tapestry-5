// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.services.ComponentClassResolver;

import java.util.Map;

public class RequestPageCacheImpl implements RequestPageCache, ThreadCleanupListener
{
    private final PagePool pagePool;

    private final ComponentClassResolver resolver;
    /**
     * Keyed on canonical page name (case insensitive).
     */
    private final Map<String, Page> cache = CollectionFactory.newCaseInsensitiveMap();

    public RequestPageCacheImpl(PagePool pagePool, ComponentClassResolver resolver)
    {
        this.pagePool = pagePool;
        this.resolver = resolver;
    }

    public Page get(String pageName)
    {
        Defense.notNull(pageName, "pageName");

        String canonicalName = resolver.canonicalizePageName(pageName);

        Page page = cache.get(canonicalName);

        if (page == null)
        {
            page = pagePool.checkout(canonicalName);

            try
            {
                page.attached();
            }
            catch (RuntimeException ex)
            {
                pagePool.discard(page);

                throw ex;
            }

            cache.put(canonicalName, page);
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
