// Copyright 2010, 2011, 2012 The Apache Software Foundation
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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentMessages;
import org.apache.tapestry5.services.ComponentTemplates;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.pageload.PageClassloaderContext;
import org.apache.tapestry5.services.pageload.PageClassloaderContextManager;
import org.slf4j.Logger;

public class PageSourceImpl implements PageSource
{
    private final ComponentRequestSelectorAnalyzer selectorAnalyzer;

    private final PageLoader pageLoader;

    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final ComponentClassResolver componentClassResolver;
    
    private final PageClassloaderContextManager pageClassloaderContextManager;
    
    private final Logger logger;
    
    final private boolean productionMode;
    
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

        @Override
        public String toString() {
            return "CachedPageKey [pageName=" + pageName + ", selector=" + selector + "]";
        }
        
        
    }

    private final Map<CachedPageKey, SoftReference<Page>> pageCache = CollectionFactory.newConcurrentMap();

    public PageSourceImpl(PageLoader pageLoader, ComponentRequestSelectorAnalyzer selectorAnalyzer,
            ComponentDependencyRegistry componentDependencyRegistry,
            ComponentClassResolver componentClassResolver,
            PageClassloaderContextManager pageClassloaderContextManager,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            Logger logger)
    {
        this.pageLoader = pageLoader;
        this.selectorAnalyzer = selectorAnalyzer;
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.componentClassResolver = componentClassResolver;
        this.productionMode = productionMode;
        this.pageClassloaderContextManager = pageClassloaderContextManager;
        this.logger = logger;
    }
    
    public Page getPage(String canonicalPageName)
    {
        return getPage(canonicalPageName, true);
    }

    public Page getPage(String canonicalPageName, boolean invalidateUnknownContext)
    {
        ComponentResourceSelector selector = selectorAnalyzer.buildSelectorForRequest();

        CachedPageKey key = new CachedPageKey(canonicalPageName, selector);

        // The while loop looks superfluous, but it helps to ensure that the Page instance,
        // with all of its mutable construction-time state, is properly published to other
        // threads (at least, as I understand Brian Goetz's explanation, it should be).

        while (true)
        {
            SoftReference<Page> ref = pageCache.get(key);

            Page page = ref == null ? null : ref.get();

            if (page != null)
            {
                return page;
            }
            
            // Avoiding problems in PlasticClassPool.createTransformation()
            // when the class being loaded has a page superclass
            final String className = componentClassResolver.resolvePageNameToClassName(canonicalPageName);
            PageClassloaderContext context = pageClassloaderContextManager.get(className);
            final Class<?> superclass = getSuperclass(className, context);
            final String superclassName = superclass.getName();
            if (componentClassResolver.isPage(superclassName)) 
            {
                getPage(componentClassResolver.resolvePageClassNameToPageName(superclassName), false);
            }

            // In rare race conditions, we may see the same page loaded multiple times across
            // different threads. The last built one will "evict" the others from the page cache,
            // and the earlier ones will be GCed.

            page = pageLoader.loadPage(canonicalPageName, selector);

            ref = new SoftReference<Page>(page);

            pageCache.put(key, ref);
            
            // TODO: remove this?
            if (!productionMode)
            {
                final ComponentPageElement rootElement = page.getRootElement();
                componentDependencyRegistry.clear(rootElement);
                componentDependencyRegistry.register(rootElement);
                context = pageClassloaderContextManager.get(className);
                if (context.isUnknown())
                {
                    componentDependencyRegistry.disableInvalidations();
                    pageClassloaderContextManager.invalidateAndFireInvalidationEvents(context);
                    componentDependencyRegistry.enableInvalidations();
                    pageClassloaderContextManager.get(className);
                    return getPage(canonicalPageName);
                }
            }
            
        }
    }

    private Class<?> getSuperclass(final String className, PageClassloaderContext context) 
    {
        try {
            return context.getClassLoader().loadClass(className).getSuperclass();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PostInjection
    public void setupInvalidation(@ComponentClasses InvalidationEventHub classesHub,
                                  @ComponentTemplates InvalidationEventHub templatesHub,
                                  @ComponentMessages InvalidationEventHub messagesHub,
                                  ResourceChangeTracker resourceChangeTracker)
    {
        classesHub.addInvalidationCallback(this::listen);
        templatesHub.addInvalidationCallback(this::listen);
        messagesHub.addInvalidationCallback(this::listen);

        // Because Assets can be injected into pages, and Assets are invalidated when
        // an Asset's value is changed (partly due to the change, in 5.4, to include the asset's
        // checksum as part of the asset URL), then when we notice a change to
        // any Resource, it is necessary to discard all page instances.
        // From 5.8.3 on, Tapestry tries to only invalidate the components and pages known as 
        // using the changed resources. If a given resource is changed but not associated with any
        // component, then all of them are invalidated.
        resourceChangeTracker.addInvalidationCallback(this::listen);
    }
    
    private List<String> listen(List<String> resources)
    {
    
        if (resources.isEmpty())
        {
            clearCache();
        }
        else
        {
            String pageName;
            for (String className : resources)
            {
                if (componentClassResolver.isPage(className))
                {
                    pageName = componentClassResolver.resolvePageClassNameToPageName(className);
                    final Iterator<Entry<CachedPageKey, SoftReference<Page>>> iterator = pageCache.entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        final Entry<CachedPageKey, SoftReference<Page>> entry = iterator.next();
                        final String entryPageName = entry.getKey().pageName;
                        if (entryPageName.equalsIgnoreCase(pageName)) 
                        {
                            logger.info("Clearing cached page '{}'", pageName);
                            iterator.remove();
                        }
                    }
                }
            }
        }
            
        return Collections.emptyList();
    }

    public void clearCache()
    {
        logger.info("Clearing page cache");
        pageCache.clear();
    }

    public Set<Page> getAllPages()
    {
        return F.flow(pageCache.values()).map(new Mapper<SoftReference<Page>, Page>()
        {
            public Page map(SoftReference<Page> element)
            {
                return element.get();
            }
        }).removeNulls().toSet();
    }
}
