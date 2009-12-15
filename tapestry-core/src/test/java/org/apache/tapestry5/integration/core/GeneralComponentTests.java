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

package org.apache.tapestry5.integration.core;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

public class GeneralComponentTests extends TapestryCoreTestCase
{
    @Test
    public void unless_component()
    {
        clickThru("Unless Demo");

        assertText("//p[@id='false']", "false is rendered");

        assertText("//p[@id='true']", "");
    }

    /**
     * TAPESTRY-2044
     */
    @Test
    public void action_links_on_non_active_page()
    {
        clickThru("Action Links off of Active Page");

        String contextSpan = "//span[@id='context']";

        assertText(contextSpan, "0");

        clickAndWait("link=3");

        assertText(contextSpan, "3");

        clickAndWait("link=refresh");

        assertText(contextSpan, "3");

        clickAndWait("link=1");

        assertText(contextSpan, "1");

        clickAndWait("link=refresh");

        assertText(contextSpan, "1");
    }
}
