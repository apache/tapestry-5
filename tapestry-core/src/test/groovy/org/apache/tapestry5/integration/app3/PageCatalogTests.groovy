package org.apache.tapestry5.integration.app3

import org.apache.tapestry5.test.SeleniumTestCase
import org.testng.annotations.Test

class PageCatalogTests extends SeleniumTestCase
{

    @Test
    void load_page_catalog_page()
    {
        open("${baseURL}pagecatalog")

        assertTitle "Tapestry Page Catalog"

        clickAndWait "link=clear the cache"

        assertTitle "Tapestry Page Catalog"

        assertTextPresent "Page cache cleared"

        clickAndWait "link=Run the Java Garbage Collector"

        assertTextPresent "Garbage collection freed"

        click "link=load all pages"
    }
}
