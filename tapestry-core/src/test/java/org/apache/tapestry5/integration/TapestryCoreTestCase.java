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
            waitForPageToLoad(PAGE_LOAD_TIMEOUT);
        }
    }
}
