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

import java.util.Locale;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocaleTest extends Assert
{
    private PageTester _tester;

    @Test
    public void no_preferred_language()
    {
        Document doc = _tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildText(), "English page");
    }

    @Test
    public void prefer_canada_french()
    {
        _tester.setPreferedLanguage(Locale.CANADA_FRENCH);
        Document doc = _tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildText(), "French page");
    }

    @Test
    public void change_language_in_browser()
    {
        Document doc = _tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildText(), "English page");
        _tester.setPreferedLanguage(Locale.CANADA_FRENCH);
        doc = _tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildText(), "French page");
    }

    @Test
    public void persist_locale()
    {
        Document doc = _tester.renderPage("TestPageForLocale");
        doc = _tester.clickLink(doc.getElementById("changeLocale"));
        assertEquals(doc.getElementById("id1").getChildText(), "French page");
    }

    @BeforeMethod
    public void before()
    {
        String appPackage = "org.apache.tapestry.integration.app2";
        // LocaleAppModule.java has configured support for a certain locales.
        String appName = "LocaleApp";
        _tester = new PageTester(appPackage, appName);
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
