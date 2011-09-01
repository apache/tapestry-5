package org.apache.tapestry5.integration.appfolder

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class AppFolderTests extends TapestryCoreTestCase
{

    /**
     * Tests basic navigation, and also tests ability to place a page template in the context, under the configured
     * application folder.
     */
    @Test
    void page_navigation()
    {
        openLinks "t5app/"

        assertTitle "Index Page"

        clickAndWait "link=context template demo"

        assertTitle "Context Template"

        clickAndWait "link=back to index"

        assertTitle "Index Page"
    }

    @Test
    void component_event_request()
    {
        openLinks "t5app/", "show index page alert"

        assertText "css=div.t-message-container", "index page alert"
    }

    @Test
    void static_pages()
    {
        openLinks "static.html"

        assertTitle "Static File"

        clickAndWait "link=to Tapestry application"

        assertTitle "Index Page"
    }
}
