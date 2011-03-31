// Copyright 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.test.TapestryTestConstants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AssetTests extends TapestryCoreTestCase
{
    @DataProvider
    private Object[][] asset_data()
    {
        return new Object[][]
        {
        { "icon", "src/test/app1/images/tapestry_banner.gif" },
        { "button", "src/test/resources/org/apache/tapestry5/integration/app1/pages/nested/tapestry-button.png" },
        { "viaContext", "src/test/app1/images/asf_logo_wide.gif" } };
    }

    @Test(dataProvider = "asset_data")
    public void assets(String id, String localPath) throws Exception
    {
        openLinks("AssetDemo");

        // Test for https://issues.apache.org/jira/browse/TAPESTRY-1935

        // assertSourcePresent("<link href=\"/css/app.css\" rel=\"stylesheet\" type=\"text/css\">");

        // Read the byte stream for the asset and compare to the real copy.

        String assetURL = getAttribute(String.format("//img[@id='%s']/@src", id));

        compareDownloadedAsset(assetURL, localPath);
    }

    private void compareDownloadedAsset(String assetURL, String localPath) throws Exception
    {
        // Strip off the leading slash

        URL url = new URL(getBaseURL() + assetURL.substring(1));

        byte[] downloaded = readContent(url);

        File local = new File(TapestryTestConstants.MODULE_BASE_DIR, localPath);

        byte[] actual = readContent(local.toURL());

        assertEquals(downloaded, actual);
    }

    private byte[] readContent(URL url) throws Exception
    {
        InputStream is = new BufferedInputStream(url.openStream());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        TapestryInternalUtils.copy(is, os);

        os.close();
        is.close();

        return os.toByteArray();
    }
}
