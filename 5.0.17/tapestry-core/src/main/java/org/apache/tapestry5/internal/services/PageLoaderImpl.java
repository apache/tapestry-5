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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.events.InvalidationListener;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.ComponentClassResolver;

import java.util.Locale;

public class PageLoaderImpl extends InvalidationEventHubImpl implements PageLoader,
        InvalidationListener
{
    private final ComponentTemplateSource templateSource;

    private final PageElementFactory pageElementFactory;

    private final LinkFactory linkFactory;

    private final PersistentFieldManager persistentFieldManager;

    private final ComponentClassResolver componentClassResolver;

    public PageLoaderImpl(ComponentTemplateSource templateSource,
                          PageElementFactory pageElementFactory, LinkFactory linkFactory,
                          PersistentFieldManager persistentFieldManager, ComponentClassResolver componentClassResolver)
    {
        this.templateSource = templateSource;
        this.pageElementFactory = pageElementFactory;
        this.linkFactory = linkFactory;
        this.persistentFieldManager = persistentFieldManager;
        this.componentClassResolver = componentClassResolver;
    }

    public Page loadPage(String logicalPageName, Locale locale)
    {
        // For the moment, the processors are used once and discarded. Perhaps it is worth the
        // effort to pool them for reuse, but not too likely.

        PageLoaderProcessor processor = new PageLoaderProcessor(templateSource,
                                                                pageElementFactory, linkFactory,
                                                                persistentFieldManager, componentClassResolver);

        String pageClassName = componentClassResolver.resolvePageNameToClassName(logicalPageName);

        return processor.loadPage(logicalPageName, pageClassName, locale);
    }

    /**
     * When the page loader receives an invalidation event, it respawns the event for its listeners. Those listeners
     * will include page caches and the like.
     */
    public void objectWasInvalidated()
    {
        fireInvalidationEvent();
    }
}
