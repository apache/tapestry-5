package org.apache.tapestry5.integration.app3

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class PageCatalogTests extends TapestryCoreTestCase
{
    @Test
    void load_page_catalog_page()
    {
        def title = "Tapestry 5: Page Catalog"

        open("${baseURL}pagecatalog")

        assertTitle title

        click "link=clear the cache"

        sleep 1000

        assertTextPresent "Page cache cleared"

        click "link=Run the GC"

        sleep 1000

        assertTextPresent "Garbage collection freed"

        click "link=load all pages"

        sleep 1000

        assertTextPresent "new pages for selector"
    }
}
