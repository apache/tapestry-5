// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.pagelevel;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ActionLinkTest extends Assert
{
    private PageTester tester;

    @Test
    public void click_link()
    {

        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);

        Document doc = tester.renderPage("TestPageForActionLink");
        Element link = doc.getElementById("link1");
        doc = tester.clickLink(link);
        assertTrue(doc.toString().contains("You chose: 123"));
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
