// Copyright 2009, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.integration;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryRunnerConstants;

public abstract class TapestryCoreTestCase extends SeleniumTestCase
{
    public static final String BACK_TO_INDEX = "link=Tapestry Integration Test Application";
    public static final String REFRESH_PAGE = "link=Refresh Page";
    public static final String EXCEPTION_PROCESSING_REQUEST = "An exception has occurred processing this request.";
    public static final String TEST_APP_BANNER = "Tapestry Integration Test Application";

    // Rule of thumb time, in ms, to wait for Ajax to occur.
    /**
     *@deprecated Use {@link #waitForAjaxRequestsToComplete()} 
     */
    @Deprecated
    public static final int AJAX_WAIT_TIME = 250;

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    protected final void assertFieldValueSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertFieldValue(id, values[i]);
        }
    }

    /**
     * Asserts that the text of the first alert matches the given value. Waits for the alerts container
     * and the alert itself to appear.
     *
     * @param text
     * @since 5.4
     */
    protected final void assertFirstAlert(String text)
    {
        waitForCssSelectorToAppear("*[data-container-type=alerts] .alert");

        // Add the special "x" for the close button to the text.
        assertText("css=[data-container-type=alerts] .alert span", text);
    }

    /**
     * Assert that asset at the given URL contains the exact same contents as the
     * file at the given path.
     * @param assetURL a root-relative (starting with "/") URL to an asset, such as
     * "/t5app/assets/ctx/b492f3dd/images/t5-logo.png"
     * @param path the path (relative to the module base directory) where the asset file exists, such
     * as "src/test/appfolder/images/filename.ext"
     * @throws IOException 
     * @since 5.5
     */
    protected final void assertDownloadedAsset(String assetURL, String path) throws IOException
    {
        URL url = new URL(getBaseURL() + assetURL.substring(1));

        byte[] downloaded = getBytes(url);

        File file = new File(TapestryRunnerConstants.MODULE_BASE_DIR, path);
        byte[] actual = Files.readAllBytes(file.toPath());
        
        assertEquals(downloaded, actual, "Asset contents differ at " + url + " and " + path);
    }

    /**
     * Read (download) the content of this URL and return it as a byte[].
     *
     * @param url URL to read content from
     * @return the byte[] from that URL
     * @throws IOException if an IOException occurs.
     */
    private static byte[] getBytes(URL url) throws IOException {
        InputStream is = new BufferedInputStream(url.openStream());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        TapestryInternalUtils.copy(is, os);

        os.close();
        is.close();

        return os.toByteArray();
    }

}
