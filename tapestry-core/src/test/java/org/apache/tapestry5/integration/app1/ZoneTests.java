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

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests related to the {@link Zone} component.
 */
public class ZoneTests extends TapestryCoreTestCase
{
    /**
     * TAP5-138
     */
    @Test
    public void select_zone()
    {
        openLinks("Select Zone Demo");

        select("carMaker", "Bmw");

        waitForElementToAppear("carModelContainer");

        click(SUBMIT);

        String condition = String.format("window.$$(\"%s\")", "t-error-popup");

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);

        assertText(String.format("//div[@class='%s']/span", "t-error-popup"), "You must provide a value for Car Model.");

        String selectLocator = "//div[@id='modelZone']//select";

        select(selectLocator, "7 Series");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: BMW");

        assertTextPresent("Car Model: 7 Series");

        select("carMaker", "Mercedes");

        waitForElementToAppear("carModelContainer");

        select(selectLocator, "E-Class");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: MERCEDES");

        assertTextPresent("Car Model: E-Class");
    }

    @Test
    public void zone_updates()
    {
        openLinks("Zone Demo");

        assertTextPresent("No name has been selected.");

        // Hate doing this, but selecting by the text isn't working, perhaps
        // because of the
        // HTML entities.
        click("select_0");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.

        // assertTextPresent("Selected: Mr. &lt;Roboto&gt;");

        click("link=Direct JSON response");
    }

    /**
     * TAP5-187
     */
    @Test
    public void zone_redirect_by_class()
    {
        openLinks("Zone Demo");

        clickAndWait("link=Perform a redirect to another page");

        assertText("activePageName", "nested/AssetDemo");
    }

    /**
     * TAP5-108
     */
    @Test
    public void update_multiple_zones_at_once()
    {
        openLinks("Multiple Zone Update Demo");

        String now = getText("now");

        assertText("wilma", "Wilma Flintstone");

        assertText("message", "");

        click("update");

        waitForElementToAppear("fredName");

        assertText("fredName", "Fred Flintstone");
        assertText("dino", "His dog, Dino.");
        assertText("wilma", "His Wife, Wilma.");

        assertText("message", "Updated");

        // Ideally, we'd add checks that the JavaScript for the Palette in the
        // Barney Zone was
        // updated.

        // Make sure it was a partial update
        assertText("now", now);
    }

    /**
     * TAP5-573
     */
    @Test
    public void zone_namespace_interaction_fixed()
    {
        openLinks("Zone/Namespace Interaction");

        String outerNow = getText("outernow");
        String innerNow = getText("innernow");

        // If we're too fast that innernow doesn't change because its all within
        // a single second.

        sleep(1050);

        click(SUBMIT);

        waitForElementToAppear("message");

        // Make sure it was just an Ajax update.
        assertEquals(getText("outernow"), outerNow);

        assertFalse(getText("innernow").equals(innerNow));
    }

    @Test
    public void zone_updated_event_triggered_on_client()
    {
        openLinks("Zone Demo");

        assertText("zone-update-message", "");

        click("link=Direct JSON response");

        // Give it some time to process.

        sleep(100);

        assertText("zone-update-message", "Zone updated.");
    }

    /**
     * TAP5-389
     */
    @Test
    public void link_submit_inside_form_that_updates_a_zone()
    {
        openLinks("LinkSubmit inside Zone");

        String now = getText("now");

        waitForElementToAppear("mySubmit");

        click("//a[@id='mySubmit']");

        waitForElementToAppear("value_errorpopup");

        type("value", "robot chicken");

        click("//a[@id='mySubmit']");

        waitForElementToAppear("outputvalue");

        assertText("outputvalue", "robot chicken");

        assertText("eventfired", "true");

        // Make sure it was a partial update
        assertText("now", now);
    }

    @Test
    public void zone_inject_component_from_template()
    {
        openLinks("Inject Component Demo");

        assertTextPresent(Form.class.getName() + "[form--form]");
    }

    /**
     * TAP5-707
     */
    @Test
    public void zone_fade_back_backgroundcolor()
    {
        openLinks("Form Zone Demo");

        type("longValue", "12");

        click(SUBMIT);

        click(SUBMIT);

        // wait some time to let the fade go away
        sleep(4050);

        // will only work in firefox.
        String color = getEval("selenium.browserbot.getCurrentWindow().getComputedStyle(this.page().findElement(\"xpath=//div[@id='valueZone']\"),'').getPropertyValue('background-color').toLowerCase()");

        assertEquals(color, "rgb(255, 255, 255)");
    }

    /**
     * TAP5-1084
     */
    @Test
    public void update_zone_inside_form()
    {
        openLinks("Zone/Form Update Demo");

        click("link=Update the form");

        waitForElementToAppear("updated");

        type("//INPUT[@type='text']", "Tapestry 5.2");

        clickAndWait(SUBMIT);

        assertText("output", "Tapestry 5.2");
    }

    /**
     * TAP5-1109
     */
    @Test
    public void update_to_zone_inside_form()
    {
        openLinks("MultiZone Update inside a Form");

        select("selectValue1", "3 pre ajax");

        waitForElementToAppear("select2ValueZone");

        select("//div[@id='select2ValueZone']//select", "4 post ajax");
    }

    @Test
    public void multi_zone_update_using_string_in_loop()
    {
        openLinks("MultiZone String Body Demo");
        String[] numbers = new String[]{
                "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"
        };

        for (int i = 0; i <= 10; i++)
        {
            assertText("row-" + i, numbers[i]);
        }

        click("click_7");
        waitForElementToAppear("row-7");

        // 7- are unchanged
        for (int i = 0; i <= 7; i++)
        {
            assertText("row-" + i, numbers[i]);
        }
        // 8+ are modified
        for (int i = 8; i <= 10; i++)
        {
            assertText("row-" + i, i + " is the integer value");
        }

        click("reset");
        waitForElementToAppear("wholeLoopZone");

        // all elements reset via AJAX
        for (int i = 0, numbersLength = numbers.length; i < numbersLength; i++)
        {
            assertText("row-" + i, numbers[i]);
        }

    }

    private void assertCSS(String elementId, String cssProperty, String expected)
    {
        // See http://groups.google.com/group/selenium-users/browse_thread/thread/f21e0a43c9913d42

        String actual = selenium.getEval(String.format("window.document.defaultView.getComputedStyle(window.document.getElementById('%s'), null).getPropertyValue('%s')",
                elementId, cssProperty));

        assertEquals(actual, expected, String.format("CSS property '%s' of '%s' should be '%s'.", cssProperty, elementId, expected));
    }

    @Test
    public void css_insertion_point()
    {
        openLinks("Zone Demo");

        click("link=Select \"CSS Injection\"");

        sleep(100);

        // First check that the update arrived

        assertText("demo-aip", "This should be styled GREEN.");

        // Next see if we can verify that the presentation matches the exceptations; greend and underlined.  Underlined from
        // zonedemo-viaajax.css; green from zonedmeo-overrides.css (not blue as defined in zonedemo-viaajax.css).


        assertCSS("demo-aip", "color", "rgb(0, 128, 0)");
        assertCSS("demo-aip", "text-decoration", "underline");
    }


    /**
    * TAP5-1890
    */
    @Test
    public void update_zone_with_empty_body()
    {
        openLinks("Zone Demo");

        assertText("zone-update-message", "");

        click("link=Update zone with empty body");

        // Give it some time to process.

        sleep(100);

        assertText("zone-update-message", "Zone updated.");
    }

}
