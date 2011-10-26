// Copyright 2011 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.func.*;
import org.apache.tapestry5.internal.PageCatalogTotals;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Lists out the currently loaded pages, using a {@link org.apache.tapestry5.corelib.components.Grid}.
 * Provides an option to force all pages to be loaded. In development mode, includes an option to clear the page cache.
 */
@ContentType("text/html")
@WhitelistAccessOnly
public class PageCatalog
{

    @Property
    private PageCatalogTotals totals;

    @Property
    @Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Inject
    private PageSource pageSource;

    @Inject
    private ComponentResourceSelector selector;

    @Inject
    private ComponentClassResolver resolver;

    @Inject
    private AlertManager alertManager;

    @Property
    private Page page;

    @InjectComponent
    private Zone pagesZone;

    @Persist
    private Set<String> failures;

    @Property
    @Validate("required")
    @Persist
    private String pageName;

    @Inject
    private OperationTracker operationTracker;

    @Inject
    private ComponentInstantiatorSource componentInstantiatorSource;

    @Inject
    private BeanModelSource beanModelSource;

    @Inject
    private Messages messages;

    @Property
    @Retain
    private BeanModel<Page> model;

    void pageLoaded()
    {
        model = beanModelSource.createDisplayModel(Page.class, messages);

        model.addExpression("selector", "selector.toString()");
        model.addExpression("assemblyTime", "stats.assemblyTime");
        model.addExpression("componentCount", "stats.componentCount");
        model.addExpression("weight", "stats.weight");

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

    Object onActionFromReloadClasses()
    {
        if (productionMode)
        {
            alertManager.error("Forcing a class reload is only allowed when executing in development mode.");
            return null;
        }

        pageName = null;

        componentInstantiatorSource.forceComponentInvalidation();

        alertManager.info("Forced a component class reload.");

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

    Object onActionFromClearCache()
    {
        if (productionMode)
        {
            alertManager.error("Clearing the cache is only allowed in development mode.");
            return null;
        }

        pageSource.clearCache();

        failures = null;

        alertManager.info("Page cache cleared.");

        return pagesZone.getBody();
    }

    Object onActionFromRunGC()
    {
        if (productionMode)
        {
            alertManager.error("Executing a garbage collection is only allowed in development mode.");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();

        long initialFreeMemory = runtime.freeMemory();

        runtime.gc();

        long delta = runtime.freeMemory() - initialFreeMemory;

        alertManager.info(String.format("Garbage collection freed %,.2f Kb of memory.",
                ((double) delta) / 1024.0d));

        return pagesZone.getBody();
    }

    public String formatElapsed(long millis)
    {
        return String.format("%,d ms", millis);
    }
}
