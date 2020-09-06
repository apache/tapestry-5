// Copyright 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.pagetester;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.http.internal.TapestryHttpInternalSymbols;
import org.apache.tapestry5.integration.pagelevel.TestConstants;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

/**
 * Tests on PageTester itself.
 */
public class PageTesterTest extends Assert
{
    private PageTester nonEmptyAppNameTester;
    
    private PageTester emptyAppNameTester;
    
    @BeforeClass
    public void setup()
    {
        nonEmptyAppNameTester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME, "src/test/app2");
        
        emptyAppNameTester = new PageTester(TestConstants.APP2_PACKAGE, "", "src/test/app2");
    }

    @AfterClass
    public void cleanup()
    {
        nonEmptyAppNameTester.shutdown();

        nonEmptyAppNameTester = null;
        
        emptyAppNameTester.shutdown();

        emptyAppNameTester = null;
    }
    
    @DataProvider(name = "testers")
    public Object[][] getTesters()
    {
        return new Object[][] { { nonEmptyAppNameTester }, { emptyAppNameTester } };
    }

    @Test(dataProvider = "testers")
    public void on_activate_chain_is_followed(PageTester tester)
    {
        Document launchDoc = tester.renderPage("Launch");

        Map<String, String> parameters = Collections.emptyMap();

        // Submit the form, which will then skip through Intermediate and
        // arrive at Final.

        Document finalDoc = tester.submitForm(launchDoc.getElementById("form"), parameters);

        assertEquals(finalDoc.getElementById("page-name").getChildMarkup(), "Final");
    }

    @Test(dataProvider = "testers")
    public void application_path_is_defined_as_a_symbol(PageTester tester)
    {
        SymbolSource source = tester.getRegistry().getService(SymbolSource.class);

        assertEquals(source.valueForSymbol(TapestryHttpInternalSymbols.APP_PACKAGE_PATH), "org/apache/tapestry5/integration/app2");
    }
}
