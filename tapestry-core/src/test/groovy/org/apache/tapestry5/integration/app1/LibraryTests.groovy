package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.testng.annotations.Test

class LibraryTests extends GroovyTapestryCoreTestCase
{

    /**
     * Tests case where a library is mapped to a subfolder (i.e., "lib/alpha"). This was not allowed in 5.2,
     * but is added back in for 5.3.
     */
    @Test
    void access_to_assets_in_library_with_subfolder()
    {
        openLinks "Alpha Library Root"

        String assetURL = getAttribute("//img[@id='t5logo']/@src")

        assert assetURL.contains("lib/alpha/pages")

        assertDownloadedAsset assetURL, "src/test/resources/org/apache/tapestry5/integration/locallib/alpha/pages/tapestry.png"
    }

}
