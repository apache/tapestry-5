package org.apache.tapestry5.integration.appfolder

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/appfolder")
class AppFolderTests extends GroovyTapestryCoreTestCase
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

        assertTextPresent "index page alert"
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
    void asset_access()
    {
        openLinks "t5app/"

        // there's only one image on page
        String assetURL = getAttribute("//img/@src")

        // Selenium now (sometimes?) adds unwanted port & host
        if (assetURL.startsWith("http")) {
            assetURL = new URL(assetURL).getPath()
        }

        assert assetURL.startsWith("/t5app/assets/")

        assertDownloadedAsset assetURL, "src/test/appfolder/images/t5-logo.png"
    }
}
