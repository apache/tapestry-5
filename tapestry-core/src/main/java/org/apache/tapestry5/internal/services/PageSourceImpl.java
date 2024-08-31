// Copyright 2010, 2011, 2012, 2023, 2024 The Apache Software Foundation
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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import org.apache.tapestry5.internal.ThrowawayClassLoader;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
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
import org.apache.tapestry5.services.pageload.PageCachingReferenceTypeService;
import org.apache.tapestry5.services.pageload.PageClassLoaderContext;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;
import org.apache.tapestry5.services.pageload.ReferenceType;
import org.slf4j.Logger;

public class PageSourceImpl implements PageSource
{
    private final ComponentRequestSelectorAnalyzer selectorAnalyzer;

    private final PageLoader pageLoader;

    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final ComponentClassResolver componentClassResolver;
    
    private final PageClassLoaderContextManager pageClassLoaderContextManager;
    
    private final PageCachingReferenceTypeService pageCachingReferenceTypeService;
    
    private final Logger logger;
    
    final private boolean productionMode;
    
    final private boolean multipleClassLoaders;
    
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

    private final Map<CachedPageKey, Object> pageCache = CollectionFactory.newConcurrentMap();
    
    private final Map<String, Boolean> abstractClassInfoCache = CollectionFactory.newConcurrentMap();
    
    private final static ThreadLocal<String> CURRENT_PAGE = 
            ThreadLocal.withInitial(() -> null);
    
    private final static ThreadLocal<Set<String>> CALL_STACK = 
            ThreadLocal.withInitial(HashSet::new);

    public PageSourceImpl(PageLoader pageLoader, ComponentRequestSelectorAnalyzer selectorAnalyzer,
            ComponentDependencyRegistry componentDependencyRegistry,
            ComponentClassResolver componentClassResolver,
            PageClassLoaderContextManager pageClassLoaderContextManager,
            PageCachingReferenceTypeService pageCachingReferenceTypeService,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS) boolean multipleClassLoaders,
            Logger logger)
    {
        this.pageLoader = pageLoader;
        this.selectorAnalyzer = selectorAnalyzer;
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.componentClassResolver = componentClassResolver;
        this.productionMode = productionMode;
        this.pageCachingReferenceTypeService = pageCachingReferenceTypeService;
        this.multipleClassLoaders = multipleClassLoaders && !productionMode;
        this.pageClassLoaderContextManager = pageClassLoaderContextManager;
        this.logger = logger;
    }
    
    public Page getPage(String canonicalPageName)
    {
        if (!productionMode)
        {
            componentDependencyRegistry.disableInvalidations();
        }
        try
        {
            @SuppressWarnings("unchecked")
            Set<String> alreadyProcessed = multipleClassLoaders ? new HashSet<>() : Collections.EMPTY_SET;
            return getPage(canonicalPageName, true, alreadyProcessed);
        }
        finally
        {
            if (!productionMode)
            {
                componentDependencyRegistry.enableInvalidations();
            }
        }
    }

    public Page getPage(String canonicalPageName, boolean invalidateUnknownContext, Set<String> alreadyProcessed)
    {
        ComponentResourceSelector selector = selectorAnalyzer.buildSelectorForRequest();

        CachedPageKey key = new CachedPageKey(canonicalPageName, selector);

        // The while loop looks superfluous, but it helps to ensure that the Page instance,
        // with all of its mutable construction-time state, is properly published to other
        // threads (at least, as I understand Brian Goetz's explanation, it should be).
        
        while (true)
        {
            
            Page page;
            Object object = pageCache.get(key);
            
            page = toPage(object);

            if (page != null)
            {
                return page;
            }
            
            final String className = componentClassResolver.resolvePageNameToClassName(canonicalPageName);
            if (multipleClassLoaders)
            {
                
                if (canonicalPageName.equals(CURRENT_PAGE.get()))
                {
                    throw new IllegalStateException("Infinite method loop detected. Bailing out.");
                }
                else
                {
                    CURRENT_PAGE.set(canonicalPageName);
                }
            
                // Avoiding problems in PlasticClassPool.createTransformation()
                // when the class being loaded has a page superclass
                final List<String> pageDependencies = getPageDependencies(className);
                CALL_STACK.get().add(className);
                
                for (String dependencyClassName : pageDependencies)
                {
                    // Avoiding infinite recursion caused by circular dependencies
                    if (!alreadyProcessed.contains(dependencyClassName) &&
                            !CALL_STACK.get().contains(className))
                    {
                        alreadyProcessed.add(dependencyClassName);
                        
                        // Avoiding infinite recursion when, through component overriding,
                        // a dependency resolves to the same canonical page name as the
                        // one already requested in this call.
                        final String dependencyPageName = componentClassResolver.resolvePageClassNameToPageName(dependencyClassName);
                        final String resolvedDependencyPageClass = componentClassResolver.resolvePageNameToClassName(dependencyPageName);
                        if (!canonicalPageName.equals(dependencyPageName)
                                && !className.equals(resolvedDependencyPageClass)
                                && !isAbstract(dependencyClassName))
                        {
                            page = getPage(dependencyPageName, 
                                    invalidateUnknownContext, alreadyProcessed);
                        }
                    }
                }
                
            }

            // In rare race conditions, we may see the same page loaded multiple times across
            // different threads. The last built one will "evict" the others from the page cache,
            // and the earlier ones will be GCed.

            page = pageLoader.loadPage(canonicalPageName, selector);

            final ReferenceType referenceType = pageCachingReferenceTypeService.get(canonicalPageName);
            if (referenceType.equals(ReferenceType.SOFT))
            {
                pageCache.put(key, new SoftReference<Page>(page));
            }
            else
            {
                pageCache.put(key, page);
            }
            
            if (!productionMode)
            {
                final ComponentPageElement rootElement = page.getRootElement();
                componentDependencyRegistry.clear(rootElement);
                componentDependencyRegistry.register(rootElement.getComponent().getClass());
                PageClassLoaderContext context = pageClassLoaderContextManager.get(className);
                
                if (context.isUnknown() && multipleClassLoaders)
                {
                    this.pageCache.remove(key);
                    if (invalidateUnknownContext)
                    {
                        pageClassLoaderContextManager.invalidateAndFireInvalidationEvents(context);
                        getPageDependencies(className);
                    }
                    context.getClassNames().clear();
                    // Avoiding bad invalidations
                    return getPage(canonicalPageName, false, alreadyProcessed);
                }
            }
            
        }
        
        
    }

    private List<String> getPageDependencies(final String className) {
        final List<String> pageDependencies = new ArrayList<>();
        pageDependencies.addAll(
                new ArrayList<String>(componentDependencyRegistry.getDependencies(className, DependencyType.INJECT_PAGE)));
        pageDependencies.addAll(
                new ArrayList<String>(componentDependencyRegistry.getDependencies(className, DependencyType.SUPERCLASS)));
        
        final Iterator<String> iterator = pageDependencies.iterator();
        while (iterator.hasNext())
        {
            final String dependency = iterator.next();
            if (!dependency.contains(".pages.") && !dependency.equals(className))
            {
                iterator.remove();
            }
        }
        
        return pageDependencies;
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
                    final Iterator<Entry<CachedPageKey, Object>> iterator = pageCache.entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        final Entry<CachedPageKey, Object> entry = iterator.next();
                        final String entryPageName = entry.getKey().pageName;
                        if (entryPageName.equalsIgnoreCase(pageName)) 
                        {
                            logger.info("Clearing cached page '{}'", pageName);
                            iterator.remove();
                        }
                    }
                    abstractClassInfoCache.remove(className);
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
        return F.flow(pageCache.values()).map(new Mapper<Object, Page>()
        {
            public Page map(Object object)
            {
                return toPage(object);
            }
        }).removeNulls().toSet();
    }
    
    private Page toPage(Object object) 
    {
        Page page;
        if (object instanceof SoftReference)
        {
            @SuppressWarnings("unchecked")
            SoftReference<Page> ref = (SoftReference<Page>) object;
            page = ref == null ? null : ref.get();
        }
        else
        {
            page = (Page) object;
        }
        return page;
    }
    
    private boolean isAbstract(final String className)
    {
        final Boolean computeIfAbsent = abstractClassInfoCache.computeIfAbsent(className, 
                (s) -> Modifier.isAbstract(ThrowawayClassLoader.load(className).getModifiers()));
        return computeIfAbsent;
    }
    
}
