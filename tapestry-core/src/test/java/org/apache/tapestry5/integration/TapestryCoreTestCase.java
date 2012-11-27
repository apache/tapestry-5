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

    /**
     * Number of milliseconds to sleep after the page has loaded, when giving JavaScript a chance to fully initialize.
     * Perhaps we need another option, say one that sets a flag on the HTML element once the initializations are complete.
     */
    public static final int SETUP_TIME = 100;

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    protected final void assertBubbleMessage(String fieldId, String expected)
    {
        String popupId = fieldId + "_errorpopup";

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

    /**
     * Asserts that the text of the first alert matches the given value. Waits for the alerts container
     * and the alert itself to appear.
     *
     * @param text
     */
    protected final void assertFirstAlert(String text)
    {
        waitForCSSSelectedElementToAppear("[data-container-type=alerts] .alert");

        // Add the special "x" for the close button to the text.
        assertText("css=[data-container-type=alerts] .alert", "\u00d7" + text);
    }
}
