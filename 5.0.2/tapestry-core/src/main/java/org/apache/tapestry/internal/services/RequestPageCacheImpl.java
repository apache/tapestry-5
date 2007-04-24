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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Map;

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;
import org.apache.tapestry.services.ComponentClassResolver;

public class RequestPageCacheImpl implements RequestPageCache, ThreadCleanupListener
{
    private final ComponentClassResolver _resolver;

    private final PagePool _pagePool;

    /**
     * Keyed on fully qualified page class name.
     */
    private final Map<String, Page> _cache = newMap();

    public RequestPageCacheImpl(ComponentClassResolver resolver, PagePool pagePool)
    {
        _resolver = resolver;
        _pagePool = pagePool;
    }

    public Page get(String pageName)
    {
        String className = _resolver.resolvePageNameToClassName(pageName);

        if (className == null)
            throw new IllegalArgumentException(ServicesMessages.pageDoesNotExist(pageName));

        return getByClassName(className);
    }

    public Page getByClassName(String className)
    {
        Page page = _cache.get(className);

        if (page == null)
        {
            page = _pagePool.checkout(className);

            page.attached();

            _cache.put(className, page);
        }

        return page;
    }

    public void threadDidCleanup()
    {
        for (Page p : _cache.values())
            _pagePool.release(p);
    }
}
