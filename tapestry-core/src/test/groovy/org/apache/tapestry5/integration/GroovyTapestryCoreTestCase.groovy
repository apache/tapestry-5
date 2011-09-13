package org.apache.tapestry5.integration

import org.apache.tapestry5.test.TapestryTestConstants

class GroovyTapestryCoreTestCase extends TapestryCoreTestCase
{

    protected final assertDownloadedAsset(String assetURL, String path)
    {

        URL url = new URL(getBaseURL() + assetURL.substring(1))

        byte[] downloaded = url.bytes

        byte[] actual = new File(TapestryTestConstants.MODULE_BASE_DIR, path).bytes

        assertEquals downloaded, actual, "Contents of $path do not match"
    }
}
