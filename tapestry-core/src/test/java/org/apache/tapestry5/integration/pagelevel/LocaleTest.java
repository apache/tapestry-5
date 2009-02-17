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
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

public class LocaleTest extends Assert
{
    private PageTester tester;

    @Test
    public void no_preferred_language()
    {
        Document doc = tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildMarkup(), "English page");
    }

    @Test
    public void prefer_canada_french()
    {
        tester.setPreferedLanguage(Locale.CANADA_FRENCH);
        Document doc = tester.renderPage("TestPageForLocale");
        assertEquals(doc.getElementById("id1").getChildMarkup(), "French page");
    }

    @Test
    public void change_language_in_browser()
    {
        tester.setPreferedLanguage(Locale.ENGLISH);

        Document doc = tester.renderPage("TestPageForLocale");

        assertEquals(doc.getElementById("id1").getChildMarkup(), "English page");

        tester.setPreferedLanguage(Locale.CANADA_FRENCH);

        doc = tester.renderPage("TestPageForLocale");

        assertEquals(doc.getElementById("id1").getChildMarkup(), "French page");
    }

    @BeforeMethod
    public void before()
    {
        // LocaleAppModule.java has configured support for a certain locales.
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);
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
