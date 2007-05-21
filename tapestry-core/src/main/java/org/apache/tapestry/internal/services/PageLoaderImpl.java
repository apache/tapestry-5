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

import java.util.Locale;

import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.PersistentFieldManager;

public class PageLoaderImpl extends InvalidationEventHubImpl implements PageLoader,
        InvalidationListener
{
    private final ComponentTemplateSource _templateSource;

    private final PageElementFactory _pageElementFactory;

    private final LinkFactory _linkFactory;

    private final PersistentFieldManager _persistentFieldManager;

    public PageLoaderImpl(ComponentTemplateSource templateSource,
            PageElementFactory pageElementFactory, LinkFactory linkFactory,
            PersistentFieldManager persistentFieldManager)
    {
        _templateSource = templateSource;
        _pageElementFactory = pageElementFactory;

        _linkFactory = linkFactory;
        _persistentFieldManager = persistentFieldManager;
    }

    /**
     * For the moment, this service is a singleton. However, only a single page can be built at one
     * time. The coming rework will shift the loc al variables to a secondary process object and
     * allow the loader to work in parallel.
     */
    public Page loadPage(String pageClassName, Locale locale)
    {
        // For the moment, the processors are used once and discarded. Perhaps it is worth the
        // effort to pool them for reuse, but not too likely.

        PageLoaderProcessor processor = new PageLoaderProcessor(_templateSource,
                _pageElementFactory, _linkFactory, _persistentFieldManager);

        return processor.loadPage(pageClassName, locale);
    }

    /**
     * When the page loader receives an invalidation event, it respawns the event for its listeners.
     * Those listeners will include page caches and the like.
     */
    public void objectWasInvalidated()
    {
        fireInvalidationEvent();
    }

}
