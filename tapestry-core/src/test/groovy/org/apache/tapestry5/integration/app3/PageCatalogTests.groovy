package org.apache.tapestry5.integration.app3

import org.apache.tapestry5.test.SeleniumTestCase
import org.testng.annotations.Test

class PageCatalogTests extends SeleniumTestCase
{

    @Test
    void load_page_catalog_page()
    {
        def title = "Tapestry 5: Page Catalog"

        open("${baseURL}pagecatalog")

        assertTitle title

        clickAndWait "link=clear the cache"

        assertTitle title

        assertTextPresent "Page cache cleared"

        clickAndWait "link=Run the GC"

        assertTextPresent "Garbage collection freed"

        click "link=load all pages"
    }
}
