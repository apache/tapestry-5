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

package org.apache.tapestry.integration.pagelevel;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class SubmitTest extends Assert
{
    private PageTester _tester;

    private Document _doc;

    private Map<String, String> _fieldValues;

    @Test
    public void submit_form()
    {
        Element submitButton = _doc.getElementById("capitalize1");
        _fieldValues.put("t1", "hello");
        _doc = _tester.clickSubmit(submitButton, _fieldValues);
        assertTrue(_doc.toString().contains("Value is: HELLO"));
    }

    @Test
    public void access_following_fields()
    {
        Element submitButton = _doc.getElementById("capitalize2");
        _fieldValues.put("t2", "world");
        _doc = _tester.clickSubmit(submitButton, _fieldValues);
        assertTrue(_doc.toString().contains("Value is: WORLD"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void not_a_submit()
    {
        Element submitButton = _doc.getElementById("t1");
        _tester.clickSubmit(submitButton, _fieldValues);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void not_in_form()
    {
        Element submitButton = _doc.getElementById("orphanedSubmit");
        _tester.clickSubmit(submitButton, _fieldValues);
    }

    @BeforeMethod
    public void before()
    {
        String appPackage = "org.apache.tapestry.integration.app2";
        String appName = "";
        _tester = new PageTester(appPackage, appName);
        _doc = _tester.renderPage("TestPageForSubmit");
        _fieldValues = CollectionFactory.newMap();
    }

    @AfterMethod
    public void after()
    {
        if (_tester != null)
        {
            _tester.shutdown();
        }
    }
}
