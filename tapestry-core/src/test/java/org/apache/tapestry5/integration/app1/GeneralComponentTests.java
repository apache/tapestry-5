// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.net.URL;

public class GeneralComponentTests extends App1TestCase
{
    @Test
    public void unless_component()
    {
        openLinks("Unless Demo");

        assertText("//p[@id='false']", "false is rendered");

        assertText("//p[@id='true']", "");
    }

    /**
     * TAPESTRY-2044
     */
    @Test
    public void action_links_on_non_active_page()
    {
        openLinks("Action Links off of Active Page");

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

    /**
     * TAPESTRY-2333
     */
    @Test
    public void action_links_on_custom_url()
    {
        open(getBaseURL() + "nested/actiondemo/");

        clickAndWait("link=2");

        assertTextPresent("Number: 2");
    }
    
    /**
     * TAP5-1075
     */
    @Test
    public void report_location_of_unavailable_component_in_error()
    {
        openLinks("Report Location of Unavailable Component");

        assertText("//td[@class='t-location-content t-location-current']", "<t:unavailablecomponent/>");
    }
    
    /** TAP5-1378 */
    @Test public void using_delegate_for_inline_components() {
        openLinks("Inline Delegate");
        
        // no params
        assertText("block1", "block 1");
        
        // multiple renders w/ multiple parameters
        assertText("xpath=(//p[@class='superhero'])[1]", "Steve Rogers");
        assertText("xpath=(//p[@class='superhero'])[2]", "Bruce Banner");
    }
    
    /** TAP5-742 */
    @Test public void component_tracing_comments() throws Exception {
        String contents = IOUtils.toString(new URL(getBaseURL()).openStream());
        
        // off by default
        assertFalse(contents.contains("Index:loop"));
        assertFalse(contents.contains("Index:pagelink"));
        
        // enable with a query parameter
        contents = IOUtils.toString(new URL(getBaseURL() + "?t:component-trace=true").openStream());
        assertTrue(contents.contains("Index:loop"));
        assertTrue(contents.contains("Index:pagelink"));
    }
}
