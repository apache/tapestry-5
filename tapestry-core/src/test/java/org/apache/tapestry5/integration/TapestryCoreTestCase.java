// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration;

import org.apache.tapestry5.test.SeleniumTestCase;

public abstract class TapestryCoreTestCase extends SeleniumTestCase
{
    public static final String SUBMIT = "//input[@type='submit']";

    /**
     * Opens the base URL, then clicks through a series of links to get to a desired application
     * state.
     */
    protected final void clickThru(String... linkText)
    {
        openBaseURL();

        for (String text : linkText)
        {
            click("link=" + text);
            waitForPageToLoad();
        }
    }

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    protected final void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException ex)
        {
            // Ignore.
        }
    }

    protected void waitForCSSSelectedElementToAppear(String cssRule)
    {
        String condition = String.format("window.$$(\"%s\").size() > 0", cssRule);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    protected final void waitForElementToAppear(String elementId)
    {

        String condition = String.format("window.$(\"%s\")", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    protected final void waitForElementToDisappear(String elementId)
    {
        String condition = String.format("window.$(\"%s\").hide()", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    protected final void assertBubbleMessage(String fieldId, String expected)
    {
        String popupId = fieldId + ":errorpopup";

        waitForElementToAppear(popupId);

        assertText(String.format("//div[@id='%s']/span", popupId), expected);
    }

    protected final void assertFieldValueSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertFieldValue(id, values[i]);
        }
    }
}
