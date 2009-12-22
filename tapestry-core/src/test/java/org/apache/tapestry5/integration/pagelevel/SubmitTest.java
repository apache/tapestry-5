// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class SubmitTest extends Assert
{
    private PageTester tester;

    private Document doc;

    private Map<String, String> fieldValues;

    @Test
    public void submit_form()
    {
        Element submitButton = doc.getElementById("capitalize1");
        fieldValues.put("t1", "hello");
        doc = tester.clickSubmit(submitButton, fieldValues);
        assertTrue(doc.toString().contains("Value is: HELLO"));
    }

    @Test
    public void access_following_fields()
    {
        Element submitButton = doc.getElementById("capitalize2");
        fieldValues.put("t2", "world");
        doc = tester.clickSubmit(submitButton, fieldValues);
        assertTrue(doc.toString().contains("Value is: WORLD"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void not_a_submit()
    {
        Element submitButton = doc.getElementById("t1");
        tester.clickSubmit(submitButton, fieldValues);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void not_in_form()
    {
        Element submitButton = doc.getElementById("orphanedSubmit");
        tester.clickSubmit(submitButton, fieldValues);
    }

    @BeforeMethod
    public void before()
    {
        String appPackage = "org.apache.tapestry5.integration.app2";
        String appName = "";
        tester = new PageTester(appPackage, appName);
        doc = tester.renderPage("TestPageForSubmit");
        fieldValues = CollectionFactory.newMap();
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
