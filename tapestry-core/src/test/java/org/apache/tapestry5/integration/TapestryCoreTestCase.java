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
    /**
     * 15 seconds
     */
    public static final String PAGE_LOAD_TIMEOUT = "15000";

    public static final String SUBMIT = "//input[@type='submit']";

    /**
     * Click a link identified by a locator, then wait for the resulting page to load.
     * This is not useful for Ajax updates, just normal full-page refreshes.
     * 
     * @param locator
     *            identifies the link to click
     */
    protected final void clickAndWait(String locator)
    {
        click(locator);

        waitForPageToLoad();
    }

    /**
     * Waits for the page to load (up to 15 seconds). This is invoked after clicking on an element
     * that forces a full page refresh.
     */
    protected final void waitForPageToLoad()
    {
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

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

    /**
     * Used when the locator identifies an attribute, not an element.
     * 
     * @param locator
     *            identifies the attribute whose value is to be asserted
     * @param expected
     *            expected value for the attribute
     */
    protected final void assertAttribute(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = getAttribute(locator);
        }
        catch (RuntimeException ex)
        {
            System.err.printf("Error accessing %s: %s, in:\n\n%s\n\n", locator, ex.getMessage(),
                    getHtmlSource());

            throw ex;
        }

        if (actual.equals(expected))
            return;

        System.err.printf("Text for attribute %s should be '%s' but is '%s', in:\n\n%s\n\n",
                locator, expected, actual, getHtmlSource());

        throw new AssertionError(String.format("%s was '%s' not '%s'", locator, actual, expected));
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

    protected final void assertFieldValue(String locator, String expected)
    {
        try
        {
            assertEquals(getValue(locator), expected);
        }
        catch (AssertionError ex)
        {
            System.err.printf("%s:\n%s\n\n", ex.getMessage(), getHtmlSource());

            throw ex;
        }
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
}
