// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

/**
 * Tests for immediate mode (aka T4 mode, aka {@linkplain org.apache.tapestry5.SymbolConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS
 * redirects suppressed} mode).
 */
@Test(timeOut = 50000, sequential = true, groups = { "integration" })
public class ImmediateModeTests extends AbstractIntegrationTestSuite
{
    public ImmediateModeTests()
    {
        super("src/test/app4");
    }

    @Test
    public void action_link()
    {
        start("here");

        assertTextSeries("//dd[%d]", 1, "onActivate() invoked", "automatic value");

        clickAndWait("link=refresh page");

        assertTextSeries("//dd[%d]", 1, "onActivate(String) invoked - onActivate() invoked", "automatic value");

        clickAndWait("link=refresh via action");

        // The last onActivate() is due to the "default" rendering of the page for the action link (on the same page).

        assertTextSeries("//dd[%d]", 1, "onActivate(String) invoked - onActivate() invoked - onActivate() invoked",
                         "automatic value");
    }

    @Test
    public void form_submission()
    {
        open(BASE_URL);

        type("input", "immediate mode");

        clickAndWait(SUBMIT);

        assertTextSeries("//dd[%d]", 1, "onActivate() invoked", "immediate mode");

        clickAndWait("link=refresh page");

        assertTextSeries("//dd[%d]", 1, "onActivate(String) invoked - onActivate() invoked", "immediate mode");

        clickAndWait("link=refresh via action");

        // The last onActivate() is due to the "default" rendering of the page for the action link (on the same page).

        assertTextSeries("//dd[%d]", 1, "onActivate(String) invoked - onActivate() invoked - onActivate() invoked",
                         "immediate mode");

    }


}
