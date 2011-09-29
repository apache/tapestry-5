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
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.Collection;

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

    public Collection<Page> getPages()
    {
        return pageSource.getAllPages();
    }

    void onActionFromForceLoad()
    {

        int startCount = getPages().size();

        for (String name : resolver.getPageNames())
        {
            pageSource.getPage(name);
        }

        int added = getPages().size() - startCount;

        alertManager.info(String.format("Loaded %,d new pages for selector '%s'.", added, selector.toShortString()));
    }

    void onActionFromClearCache()
    {
        if (productionMode)
        {
            alertManager.error("Clearing the cache is not allowed in production mode");
            return;
        }

        pageSource.clearCache();

        alertManager.info("Page cache cleared.");
    }

    public String formatElapsed(long millis)
    {
        return String.format("%,d ms", millis);
    }
}
