// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

public class IfTest extends Assert
{
    private PageTester tester;

    @Test
    public void render()
    {
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);

        Document doc = tester.renderPage("TestPageForIf");
        assertNotNull(doc.getElementById("1"));
        assertNotNull(doc.getElementById("3"));
        assertNotNull(doc.getElementById("5"));
        assertNotNull(doc.getElementById("8"));
        assertNull(doc.getElementById("2"));
        assertNull(doc.getElementById("4"));
        assertNull(doc.getElementById("6"));
        assertNull(doc.getElementById("7"));
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
