package org.apache.tapestry5.integration.app3

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class PageCatalogTests extends TapestryCoreTestCase
{
    /** There's not a lot we can do, because some of the pages in the test application have deliberate errors. */
    @Test
    void load_page_catalog_page()
    {
        open("${baseURL}t5dashboard/pages")

        waitForPageInitialized()

        click "link=Clear the cache"

        sleep 1000

        assertTextPresent "Page cache cleared"

        click "link=Run the GC"

        sleep 1000

        assertTextPresent "Garbage collection freed"

        click "link=Load all pages"

        // Ignore any errors that occur.
    }
}
