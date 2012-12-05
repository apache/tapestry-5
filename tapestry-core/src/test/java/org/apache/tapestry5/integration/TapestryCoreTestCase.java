// Copyright 2009, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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
    public static final String BACK_TO_INDEX = "link=Tapestry Integration Test Application";
    public static final String REFRESH_PAGE = "link=Refresh Page";
    public static final String EXCEPTION_PROCESSING_REQUEST = "An exception has occurred processing this request.";
    public static final String TEST_APP_BANNER = "Tapestry Integration Test Application";

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    protected final void assertFieldValueSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertFieldValue(id, values[i]);
        }
    }

    /**
     * Asserts that the text of the first alert matches the given value. Waits for the alerts container
     * and the alert itself to appear.
     *
     * @param text
     * @since 5.4
     */
    protected final void assertFirstAlert(String text)
    {
        waitForCSSSelectedElementToAppear("[data-container-type=alerts] .alert");

        // Add the special "x" for the close button to the text.
        assertText("css=[data-container-type=alerts] .alert", "\u00d7" + text);
    }

    /**
     * Waits for page initialization to finish, which is recognized by the {@code data-page-loaded} attribute
     * being added to the HTML element. Polls at 20ms intervals for 200ms.
     *
     * @since 5.4
     */
    protected final void waitForPageLoaded()
    {
        for (int i = 0; i < 10; i++)
        {
            if (isElementPresent("css=html[data-page-loaded]"))
            {
                return;
            }

            sleep(20);
        }

        reportAndThrowAssertionError("Page did not finish loading.");
    }
}
