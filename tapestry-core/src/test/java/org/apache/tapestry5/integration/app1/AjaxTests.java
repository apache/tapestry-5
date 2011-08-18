// Copyright 2009, 2010, 2011 The Apache Software Foundation
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
        openLinks("Autocomplete Mixin Demo");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.
    }

    @Test
    public void form_fragment()
    {
        openLinks("Form Fragment Demo", "Clear");

        type("name", "Fred");

        // Put a value into the sub field, then hide it ...
        type("sub", "subvalue");

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

        type("sub", "subvalue");

        waitForCondition("selenium.browserbot.getCurrentWindow().$('code').isDeepVisible() == true", PAGE_LOAD_TIMEOUT);

        type("name", "Barney");
        type("email", "rubble@bedrock.gov");
        type("code", "ABC123");

        click("off");

        click("subVisible");
        
        waitForCondition("selenium.browserbot.getCurrentWindow().$('code').isDeepVisible() == false", PAGE_LOAD_TIMEOUT);

        clickAndWait(SUBMIT);

        assertText("name", "Barney");
        assertText("email", "rubble@bedrock.gov");
        assertText("code", "");
        
        // .. but it still gets submitted, thanks to alwyassubmit=true
        assertText("sub", "subvalue");
    }

    //TAP5-1551 - triggering hide on the inner fragment was also hiding the outer fragment
    @Test
    public void nested_form_fragment()
    {
        openLinks("Nested Form Fragment Demo");
        assertTrue(isVisible("outertext1"));
        assertTrue(isVisible("innertext1"));
        assertTrue(isChecked("innertrigger1"));
        click("innertrigger1");
        String condition = "selenium.browserbot.getCurrentWindow().$('innertrigger1').isDeepVisible() == false";
        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
        assertTrue(isVisible("outertext1"));
		
        //now make sure that hide_and_remove is properly handled, as well...
        assertTrue(isVisible("outertext2"));
        assertTrue(isVisible("innertext2"));
        click("innertrigger2");
        condition="!(selenium.browserbot.getCurrentWindow().$('innertrigger2'))";
        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
        assertFalse(isElementPresent("innertext2"));
        assertTrue(isElementPresent("outertext2"));
    }

    @Test
    public void form_fragment_explicit_visible_bounds()
    {
        openLinks("Form Fragment Explicit Visible Bounds Demo");

        //make sure we're on tab 1...
        switchTabs(1);
        type("value1", "Some text to check");
        switchTabs(2);

        type("value2", "Some other text to check");

        clickAndWait("saveform");

        assertTrue(isVisible("tab2"));

        assertFieldValue("value2", "Some other text to check");

        switchTabs(1);

        assertFieldValue("value1", "Some text to check");

        //make sure they don't save if not revealed...
        type("value1", "new text that shouldn't save");
        click("frag1check");
        waitForInvisible("value1");

        clickAndWait("saveform");
        assertTrue(isVisible("tab1"));
        assertFalse(isVisible("value1"));
        click("frag1check");
        waitForVisible("value1");
        assertFieldValue("value1", "Some text to check");

        switchTabs(2);
        assertFieldValue("value2", "Some other text to check");
        clickAndWait("link=Clear Saved State");
    }

    @Test
    public void form_fragment_visible_bound_validation()
    {
        openLinks("Form Fragment Explicit Visible Bounds Demo");
        //make sure we're on the correct tab...
        switchTabs(1);
        //and that value1 is blank...
        type("value1", "");
        assertFieldValue("value1", "");
        //submitting should result in error in value1...
        click("saveform");
        assertBubbleMessage("value1", "You must provide a value for Value1.");

        //still wind up being able to submit here b/c there's no (good) way to highlight the error in the invisible tab.
        //but the form should return with errors.
        switchTabs(2);
        clickAndWait("saveform");
        assertTextPresent("You must provide a value for Value1.");

        switchTabs(1);
        click("frag1check");
        waitForInvisible("value1");

        clickAndWait("saveform");
        assertFalse(isTextPresent("You must provide a value for Value1."));

        switchTabs(1);
        clickAndWait("saveform");
        assertFalse(isTextPresent("You must provide a value for Value1."));
        clickAndWait("link=Clear Saved State");
    }

    private void switchTabs(int tab)
    {
        click("link=Show Tab " + tab);
        waitForVisible("tab" + tab);
    }

    @Test
    public void form_injector()
    {
        openLinks("FormInjector Demo");

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
        openLinks("Zone Demo");

        click("link=Failure on the server side");

        // Wait for the console to appear

        waitForCSSSelectedElementToAppear("div.t-console div");

        assertTextPresent("Communication with the server failed: Server-side exception.");
    }

    /**
     * TAP5-544
     */
    @Test
    public void slow_ajax_load_warning()
    {
        openLinks("Slow Ajax Demo");

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
        openLinks("ProgressiveDisplay Demo");

        waitForElementToAppear("content1");
        assertText("content1", "Progressive Display content #1.");

        waitForElementToAppear("content2");
        assertText("content2", "Music Library");
    }
}
