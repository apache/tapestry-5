// Copyright 2011-2013 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.func.Reducer;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.PageCatalogTotals;
import org.apache.tapestry5.internal.services.ComponentDependencyGraphvizGenerator;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.services.ReloadHelper;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

/**
 * Lists out the currently loaded pages, using a {@link org.apache.tapestry5.corelib.components.Grid}.
 * Provides an option to force all pages to be loaded. In development mode, includes an option to clear the page cache.
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class PageCatalog
{

    @Property
    private PageCatalogTotals totals;

    @Property
    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Inject
    private PageSource pageSource;

    @Inject
    private ComponentResourceSelector selector;

    @Inject
    private ComponentClassResolver resolver;
    
    @Inject
    private ComponentDependencyRegistry componentDependencyRegistry;

    @Inject
    private AlertManager alertManager;

    @Property
    private Page page;

    @Property
    private Page selectedPage;

    @Property
    private String dependency;

    @InjectComponent
    private Zone pagesZone;
    
    @InjectComponent
    private Zone pageStructureZone;

    @Persist
    private Set<String> failures;

    @Property
    @Validate("required")
    @Persist
    private String pageName;

    @Inject
    private OperationTracker operationTracker;

    @Inject
    private ReloadHelper reloadHelper;

    @Inject
    private BeanModelSource beanModelSource;
    
    @Inject
    private Messages messages;

    @Property
    public static BeanModel<Page> model;

    @Inject 
    private Request request;
    
    @Inject
    @ComponentClasses 
    private InvalidationEventHub classesInvalidationEventHub;
    
    @Inject
    private JavaScriptSupport javaScriptSupport;
    
    @Inject
    private ComponentDependencyGraphvizGenerator componentDependencyGraphvizGenerator;

    @Inject
    private ComponentClassResolver componentClassResolver;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;
    
    void pageLoaded()
    {
        model = beanModelSource.createDisplayModel(Page.class, messages);

        model.addExpression("selector", "selector.toString()");
        model.addExpression("assemblyTime", "stats.assemblyTime");
        model.addExpression("componentCount", "stats.componentCount");
        model.addExpression("weight", "stats.weight");
        model.add("clear", null);

        model.reorder("name", "selector", "assemblyTime", "componentCount", "weight");
    }

    public void onRecomputeTotals()
    {
        totals = new PageCatalogTotals();

        Flow<Page> pages = F.flow(getPages());

        totals.loadedPages = pages.count();
        totals.definedPages = getPageNames().size();
        totals.uniquePageNames = pages.map(new Mapper<Page, String>()
        {
            public String map(Page element)
            {
                return element.getName();
            }
        }).toSet().size();

        totals.components = pages.reduce(new Reducer<Integer, Page>()
        {
            public Integer reduce(Integer accumulator, Page element)
            {
                return accumulator + element.getStats().componentCount;
            }
        }, 0);

        Set<String> selectorIds = pages.map(new Mapper<Page, String>()
        {
            public String map(Page element)
            {
                return element.getSelector().toShortString();
            }
        }).toSet();

        totals.selectors = InternalUtils.joinSorted(selectorIds);
    }

    public List<String> getPageNames()
    {
        return resolver.getPageNames();
    }

    public Collection<Page> getPages()
    {
        return pageSource.getAllPages();
    }
    
    Object onClearPage(String className)
    {
        final String logicalName = resolver.getLogicalName(className);
        classesInvalidationEventHub.fireInvalidationEvent(Arrays.asList(className));
        alertManager.warn(String.format("Page %s (%s) has been cleared from the page cache",
                className, logicalName));
        return pagesZone.getBody();
    }
    
    Object onSuccessFromSinglePageLoad()
    {
        boolean found = !F.flow(getPages()).filter(new Predicate<Page>()
        {
            public boolean accept(Page element)
            {
                return element.getName().equals(pageName) && element.getSelector().equals(selector);
            }
        }).isEmpty();

        if (found)
        {
            alertManager.warn(String.format("Page %s has already been loaded for '%s'.",
                    pageName, selector.toShortString()));
            return null;
        }

        long startTime = System.currentTimeMillis();


        // Load the page now (may cause an exception).

        pageSource.getPage(pageName);


        alertManager.info(String.format("Loaded page %s for selector '%s' (in %,d ms).", pageName,
                selector.toShortString(), System.currentTimeMillis() - startTime));

        return pagesZone.getBody();
    }

    private class PageLoadData
    {
        int loadedCount;
        RuntimeException fail;
        boolean someFail;
    }

    Object onActionFromForceLoad()
    {
        if (failures == null)
        {
            failures = CollectionFactory.newSet();
        }

        long startTime = System.currentTimeMillis();

        final Collection<Page> initialPages = getPages();

        final PageLoadData data = new PageLoadData();

        for (final String name : resolver.getPageNames())
        {
            if (failures.contains(name))
            {
                alertManager.warn(String.format("Skipping page %s due to prior load failure.", name));
                data.someFail = true;
                continue;
            }

            operationTracker.run("Loading page " + name, new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Page newPage = pageSource.getPage(name);

                        if (!initialPages.contains(newPage))
                        {
                            data.loadedCount++;
                        }
                    } catch (RuntimeException ex)
                    {
                        alertManager.error(String.format("Page %s failed to load.", name));
                        failures.add(name);

                        if (data.fail == null)
                        {
                            pageName = name;
                            data.fail = ex;
                        }
                    }
                }
            });

            if (data.fail != null)
            {
                break;
            }
        }

        alertManager.info(String.format("Loaded %,d new pages for selector '%s' (in %,d ms).", data.loadedCount,
                selector.toShortString(), System.currentTimeMillis() - startTime));

        if (data.someFail)
        {
            alertManager.warn("Clear the cache to reset the list of failed pages.");
        }

        if (data.fail != null)
        {
            throw data.fail;
        }

        return pagesZone.getBody();
    }

    Object onActionFromClearCaches()
    {
        reloadHelper.forceReload();

        failures = null;

        return pagesZone.getBody();
    }

    Object onActionFromRunGC()
    {
        Runtime runtime = Runtime.getRuntime();

        long initialFreeMemory = runtime.freeMemory();

        runtime.gc();

        long delta = runtime.freeMemory() - initialFreeMemory;

        alertManager.info(String.format("Garbage collection freed %,.2f Kb of memory.",
                ((double) delta) / 1024.0d));

        return pagesZone.getBody();
    }

    public String formatElapsed(double millis)
    {
        return String.format("%,.3f ms", millis);
    }
    
    public List<String> getDependencies() 
    {
        List<String> dependencies = new ArrayList<>(componentDependencyRegistry.getDependencies(getSelectedPageClassName()));
        Collections.sort(dependencies);
        return dependencies;
    }
    
    public void onPageStructure(String pageName)
    {
        selectedPage = pageSource.getPage(pageName);
        ajaxResponseRenderer.addRender("pageStructureZone", pageStructureZone.getBody());
    }
    
    public String getDisplayLogicalName() 
    {
        return getDisplayLogicalName(dependency);
    }

    public String getPageClassName() 
    {
        return getClassName(page);
    }

    public String getSelectedPageClassName() 
    {
        return getClassName(selectedPage);
    }
    
    private String getClassName(Page page) 
    {
        return page.getRootComponent().getComponentResources().getComponentModel().getComponentClassName();
    }

    private String getClassName(Component component) 
    {
        return component.getComponentResources().getComponentModel().getComponentClassName();
    }

    public void onComponentTree(MarkupWriter writer) 
    {
        render(selectedPage.getRootElement(), writer);
    }
    
    private void render(ComponentPageElement componentPageElement, MarkupWriter writer) 
    {
        final Element li = writer.element("li");
        final String className = getClassName(componentPageElement.getComponent());
        final Set<String> embeddedElementIds = componentPageElement.getEmbeddedElementIds();
        
        if (componentPageElement.getComponent().getComponentResources().getComponentModel().isPage()) 
        {
            li.text(componentPageElement.getPageName());
        }
        else {
            li.text(String.format("%s (%s)", getDisplayLogicalName(className), componentPageElement.getId()));
        }
        
        if (!embeddedElementIds.isEmpty())
        {
            writer.element("ul");
            for (String id : embeddedElementIds)
            {
                render(componentPageElement.getEmbeddedElement(id), writer);
            }
            writer.end();
        }
        
        writer.end();
    }

    private String getDisplayLogicalName(final String className) 
    {
        final String logicalName = resolver.getLogicalName(className);
        String displayName = logicalName;
        if (logicalName == null || logicalName.trim().length() == 0)
        {
            if (className.contains(".base."))
            {
                displayName = "(base class)";
            }
            if (className.contains(".mixins."))
            {
                displayName = "(mixin)";
            }
        }
        return displayName;
    }

    public String getLogicalName(String className) 
    {
        return resolver.getLogicalName(className);
    }
    
    public String getGraphvizValue()
    {
        return componentDependencyGraphvizGenerator.generate(getClassName(selectedPage));
    }
    
}
