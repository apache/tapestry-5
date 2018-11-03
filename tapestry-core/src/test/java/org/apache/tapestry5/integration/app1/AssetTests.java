// Copyright 2009-2013 The Apache Software Foundation
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

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.test.TapestryRunnerConstants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class AssetTests extends App1TestCase
{
    @DataProvider
    private Object[][] asset_data()
    {
        return new Object[][]{
                {"icon", "src/test/app1/images/tapestry_banner.gif"},
                {"button", "src/test/resources/org/apache/tapestry5/integration/app1/pages/nested/tapestry-button.png"},
                {"viaContext", "src/test/app1/images/asf_logo_wide.gif"},
                {"meta", "src/test/resources/META-INF/assets/tapestry.png"},
                {"templatemeta", "src/test/resources/META-INF/assets/plugin.png"}};
    }

    @Test(dataProvider = "asset_data")
    public void assets(String id, String localPath) throws Exception
    {
        openLinks("AssetDemo");

        // Test for https://issues.apache.org/jira/browse/TAPESTRY-1935

        // assertSourcePresent("<link href=\"/css/app.css\" rel=\"stylesheet\" type=\"text/css\">");

        // Read the byte stream for the asset and compare to the real copy.

        String assetURL = getAttribute(String.format("//img[@id='%s']/@src", id));

        assertDownloadedAsset(assetURL, localPath);
    }
    
    // TAP5-1515
    @Test
    public void external_url_asset_bindings()
    {
        openLinks("AssetDemo");
        
        assertEquals("http://cdnjs.cloudflare.com/ajax/libs/d3/3.0.0/d3.js", getText("httpAsset"));
        assertEquals("https://cdnjs.cloudflare.com/ajax/libs/d3/3.0.0/d3.js", getText("httpsAsset"));
        assertEquals("http://cdnjs.cloudflare.com/ajax/libs/d3/3.0.0/d3.js", getText("protocolRelativeAsset"));
        assertEquals("ftp://cdnjs.cloudflare.com/ajax/libs/d3/3.0.0/d3.js", getText("ftpAsset"));
        
        // check whether externaly @Import'ed d3 works
        assertTrue(isElementPresent("css=svg"));
    }
    
    // TAP5-2185
    @Test
    public void redirection_of_requests_to_assets_with_wrong_checksums()
    {
        openLinks("AssetDemo");
        // paragraph is rendered with display="none" and the javascript asset changes it to display="block"
        // without the fix, selenium timesout because the javascript code that sets the condition
        // used by tapestry testing code to know when the page is finished loading is never invoked.
        assertTrue(isVisible("assetWithWrongChecksum"));
    }
}
