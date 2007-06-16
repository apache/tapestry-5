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

import org.apache.tapestry.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.PersistentFieldManager;

public class PageLoaderImpl extends InvalidationEventHubImpl implements PageLoader,
        InvalidationListener
{
    private final ComponentTemplateSource _templateSource;

    private final PageElementFactory _pageElementFactory;

    private final LinkFactory _linkFactory;

    private final PersistentFieldManager _persistentFieldManager;

    private final ComponentClassResolver _resolver;

    public PageLoaderImpl(ComponentTemplateSource templateSource,
            PageElementFactory pageElementFactory, LinkFactory linkFactory,
            PersistentFieldManager persistentFieldManager, ComponentClassResolver resolver)
    {
        _templateSource = templateSource;
        _pageElementFactory = pageElementFactory;
        _linkFactory = linkFactory;
        _persistentFieldManager = persistentFieldManager;
        _resolver = resolver;
    }

    public Page loadPage(String logicalPageName, Locale locale)
    {
        // For the moment, the processors are used once and discarded. Perhaps it is worth the
        // effort to pool them for reuse, but not too likely.

        PageLoaderProcessor processor = new PageLoaderProcessor(_templateSource,
                _pageElementFactory, _linkFactory, _persistentFieldManager);

        String pageClassName = _resolver.resolvePageNameToClassName(logicalPageName);

        return processor.loadPage(logicalPageName, pageClassName, locale);
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
