// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for various Ajax related components, mixins and behaviors.
 */
public class AjaxTests extends TapestryCoreTestCase
{
    @Test
    public void autocomplete_mixin()
    {
        clickThru("Autocomplete Mixin Demo");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.
    }

    @Test
    public void form_fragment()
    {
        clickThru("Form Fragment Demo", "Clear");

        type("name", "Fred");

        // Really, you can't type in the field because it is not visible, but
        // this checks that invisible fields are not processed.
        type("email", "this field is ignored");

        clickAndWait(SUBMIT);

        assertText("name", "Fred");
        assertText("email", "");

        clickAndWait("link=Back");
        clickAndWait("link=Clear");

        click("subscribeToEmail");
        click("on");

        waitForCondition("selenium.browserbot.getCurrentWindow().$('code').isDeepVisible() == true", PAGE_LOAD_TIMEOUT);

        type("name", "Barney");
        type("email", "rubble@bedrock.gov");
        type("code", "ABC123");

        click("off");

        waitForCondition("selenium.browserbot.getCurrentWindow().$('code').isDeepVisible() == false", PAGE_LOAD_TIMEOUT);

        clickAndWait(SUBMIT);

        assertText("name", "Barney");
        assertText("email", "rubble@bedrock.gov");
        assertText("code", "");
    }

    @Test
    public void form_injector()
    {
        clickThru("FormInjector Demo");

        assertText("sum", "0.0");

        click("link=Add a row");

        sleep(1000);

        type("//input[@type='text'][1]", "5.1");

        // I wanted to add two rows, but Selenium didn't want to play.

        clickAndWait(SUBMIT);

        assertText("sum", "5.1");

        click("link=remove");

        sleep(2000);

        clickAndWait(SUBMIT);

        assertText("sum", "0.0");
    }

    /**
     * TAP5-240
     */
    @Test
    public void ajax_server_side_exception()
    {
        clickThru("Zone Demo");

        click("link=Failure on the server side");

        // Wait for the console to appear

        waitForCSSSelectedElementToAppear("#t-console li");

        assertTextPresent("Communication with the server failed: Server-side exception.");
    }

    /**
     * TAP5-544
     */
    @Test
    public void slow_ajax_load_warning()
    {
        clickThru("Slow Ajax Demo");

        // ActionLink

        click("//a[@id='link']");

        waitForElementToAppear("slow");

        click("//a[@id='link']");

        waitForElementToAppear("zoneOutput");

        assertText("zoneOutput", "Updated via an ActionLink");

        clickAndWait("link=refresh");

        click(SUBMIT);

        waitForElementToAppear("slow");

        click(SUBMIT);

        waitForElementToAppear("zoneOutput");

        assertText("zoneOutput", "Updated via form submission.");
    }

    /**
     * TAP5-236
     */
    @Test
    public void progressive_display()
    {
        clickThru("ProgressiveDisplay Demo");

        waitForElementToAppear("content1");
        assertText("content1", "Progressive Display content #1.");

        waitForElementToAppear("content2");
        assertText("content2", "Music Library");
    }
}
