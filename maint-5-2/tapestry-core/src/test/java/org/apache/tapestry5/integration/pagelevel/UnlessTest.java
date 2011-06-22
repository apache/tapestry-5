// Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class UnlessTest extends Assert
{
    private PageTester tester;

    @Test
    public void render()
    {
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);

        Document doc = tester.renderPage("TestPageForUnless");
        assertNotNull(doc.getElementById("2"));
        assertNotNull(doc.getElementById("4"));
        assertNotNull(doc.getElementById("6"));
        assertNotNull(doc.getElementById("7"));
        assertNull(doc.getElementById("1"));
        assertNull(doc.getElementById("3"));
        assertNull(doc.getElementById("5"));
        assertNull(doc.getElementById("8"));
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
