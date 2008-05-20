// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.integration.pagelevel;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AssetTest extends Assert
{
    private PageTester tester;

    @Test
    public void submit_context_asset()
    {
        String appPackage = "org.apache.tapestry.integration.app2";
        String appName = "";
        tester = new PageTester(appPackage, appName, "src/test/app2");
        Document doc = tester.renderPage("TestPageForAsset");
        assertTrue(doc.toString().contains("test.css"));
    }

    @AfterMethod
    public void after()
    {
        if (tester != null)
        {
            tester.shutdown();
        }
    }
}
