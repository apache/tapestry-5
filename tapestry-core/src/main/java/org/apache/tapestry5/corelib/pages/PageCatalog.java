// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.Collection;
import java.util.Set;

/**
 * Lists out the currently loaded pages, using a {@link Grid}.  Provides an option to force all pages to be loaded. In development mode,
 * includes an option to clear the page cache.
 */
@ContentType("text/html")
public class PageCatalog
{
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

    public Collection<Page> getPages()
    {
        return pageSource.getAllPages();
    }

    Object onActionFromForceLoad()
    {
        if (failures == null)
        {
            failures = CollectionFactory.newSet();
        }

        long startTime = System.currentTimeMillis();

        Collection<Page> initialPages = getPages();

        int loadedCount = 0;

        RuntimeException fail = null;

        boolean someFail = false;

        for (String name : resolver.getPageNames())
        {
            if (failures.contains(name))
            {
                alertManager.warn(String.format("Skipping page %s due to prior load failure.", name));
                someFail = true;
                continue;
            }

            try
            {
                Page newPage = pageSource.getPage(name);

                if (!initialPages.contains(newPage))
                {
                    loadedCount++;
                }
            } catch (RuntimeException ex)
            {
                alertManager.error(String.format("Page %s failed to load.", name));
                failures.add(name);
                fail = ex;
                break;
            }
        }

        alertManager.info(String.format("Loaded %,d new pages for selector '%s' (in %,d ms).", loadedCount,
                selector.toShortString(), System.currentTimeMillis() - startTime));

        if (someFail)
        {
            alertManager.warn("Clear the cache to reset the list of failed pages.");
        }

        if (fail != null)
        {
            throw fail;
        }

        return pagesZone.getBody();
    }

    void onActionFromClearCache()
    {
        if (productionMode)
        {
            alertManager.error("Clearing the cache is only allowed in development mode.");
            return;
        }

        pageSource.clearCache();

        failures = null;

        alertManager.info("Page cache cleared.");
    }

    void onActionFromRunGC()
    {
        if (productionMode)
        {
            alertManager.error("Executing a garbage collection is only allowed in development mode.");
            return;
        }

        Runtime runtime = Runtime.getRuntime();

        long initialFreeMemory = runtime.freeMemory();

        runtime.gc();

        long delta = runtime.freeMemory() - initialFreeMemory;

        alertManager.info(String.format("Garbage collection freed %,.2f Kb of memory.",
                ((double) delta) / 1024.0d));
    }

    public String formatElapsed(long millis)
    {
        return String.format("%,d ms", millis);
    }
}
