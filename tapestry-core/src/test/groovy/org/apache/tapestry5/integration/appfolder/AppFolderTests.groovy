package org.apache.tapestry5.integration.appfolder

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test
import org.apache.tapestry5.test.TapestryTestConstants

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

    @Test
    void asset_access() {
        openLinks "t5app/"

        // Ony one image on page
        String assetURL = getAttribute("//img/@src")

        assert assetURL.startsWith("/t5app/assets/")

        URL url = new URL(getBaseURL() + assetURL.substring(1))

        byte[] downloaded = url.bytes

        byte[] actual = new File(TapestryTestConstants.MODULE_BASE_DIR, "src/test/appfolder/images/t5-logo.png").bytes

        assertEquals downloaded, actual, "Contents of t5-logo.png do not match"
    }
}
