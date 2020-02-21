// Copyright 2009-2013 The Apache Software Foundation
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
import org.testng.annotations.Test;

/**
 * Tests related to the {@link Zone} component.
 */
public class ZoneTests extends App1TestCase
{
    /**
     * TAP5-138
     */
    @Test
    public void select_zone()
    {
        openLinks("Select Zone Demo");

        select("carMaker", "Bmw");

        waitForAjaxRequestsToComplete();

        click(SUBMIT);

        assertTextPresent("You must provide a value for Car Model.");

        String selectLocator = "css=#modelZone select";

        select(selectLocator, "7 Series");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: BMW");

        assertTextPresent("Car Model: 7 Series");

        select("carMaker", "Mercedes");

        waitForAjaxRequestsToComplete();

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

        click("link=Select \"Mr. <Roboto>\"");

        waitForAjaxRequestsToComplete();

        assertTextPresent("Selected: Mr. <Roboto>");

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

        click("link=update");

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

        sleep(1200);

        click(SUBMIT);

        waitForAjaxRequestsToComplete();

        waitForElementToAppear("message");

        // Make sure it was just an Ajax update.
        assertText("outernow", outerNow);

        assertFalse(getText("innernow").equals(innerNow));
    }

    @Test
    public void zone_updated_event_triggered_on_client()
    {
        openLinks("Zone Demo");

        assertText("zone-update-message", "");

        click("link=Direct JSON response");

        // Give it some time to process.

        waitForAjaxRequestsToComplete();

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

        waitForAjaxRequestsToComplete();

        select("//div[@id='select2ValueZone']//select", "4 post ajax, number 013, retention policy RUNTIME");
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

        click("link=click 7");
        waitForElementToAppear("row-7");

        // 0-6 are unchanged
        for (int i = 0; i < 7; i++)
        {
            assertText("row-" + i, numbers[i]);
        }
        // 7+ are modified
        for (int i = 7; i <= 10; i++)
        {
            assertText("row-" + i, i + " is the integer value");
        }

        click("link=Reset Zones");
        waitForElementToAppear("wholeLoopZone");

        // all elements reset via AJAX
        for (int i = 0, numbersLength = numbers.length; i < numbersLength; i++)
        {
            assertText("row-" + i, numbers[i]);
        }

    }
    
    private void assertCSSStartsWith(String elementId, String cssProperty, String expected)
    {
        String actual = selenium.getEval(String.format("window.document.defaultView.getComputedStyle(window.document.getElementById('%s'), null).getPropertyValue('%s')",
                elementId, cssProperty));

        assertTrue(actual.startsWith("underline"), String.format("CSS property '%s' of '%s' should start with '%s'.", cssProperty, elementId, expected));
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

        // First check that the update arrived

        waitForElementToAppear("demo-aip");

        assertText("demo-aip", "This should be styled GREEN.");

        // Next see if we can verify that the presentation matches the exceptations; greend and underlined.  Underlined from
        // zonedemo-viaajax.css; green from zonedmeo-overrides.css (not blue as defined in zonedemo-viaajax.css).

        assertCSS("demo-aip", "color", "rgb(0, 128, 0)");
        assertCSSStartsWith("demo-aip", "text-decoration", "underline");
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

        waitForAjaxRequestsToComplete();

        assertText("zone-update-message", "Zone updated.");
    }

    /**
     * TAP5-2330
     */
    @Test
    public void update_zone_with_no_clientid()
    {
        openLinks("Zone Demo");

        assertText("zone-update-message", "");

        click("link=Update via AjaxResponseRenderer");

        waitForAjaxRequestsToComplete();

        assertText("zone-update-message", "Zone updated.");
        
        assertEquals("Selected: AjaxResponseRenderer", getText("output"));
        
    }

}
