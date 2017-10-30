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

import org.testng.annotations.Test;

/**
 * Tests for various Ajax related components, mixins and behaviors.
 */
public class AjaxTests extends App1TestCase
{
    @Test
    public void autocomplete_mixin()
    {
        openLinks("Autocomplete Mixin Demo");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.

        // also test Autocomplete with context

    }

    @Test
    // TAP5-2440
    public void autocomplete_mixin_on_required_field()
    {
        openLinks("Autocomplete Mixin Demo");

        typeKeys("required", "foo");

        clickAndWait(SUBMIT);

        assertTextPresent("required: foo");
    }

    @Test
    public void form_fragment()
    {
        openLinks("Form Fragment Demo", "Clear Errors");

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
        clickAndWait("link=Clear Errors");

        click("subscribeToEmail");
        click("on");

        // Type a value into the "always submit" field ...
        type("sub", "subvalue");

        // And the other fields ...
        type("name", "Barney");
        type("email", "rubble@bedrock.gov");
        type("code", "ABC123");

        // Now turn off the fragment for the "code" field
        click("off");

        // And hide the subcode fields (but they still always submit)
        click("subVisible");

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

        assertTrue(isVisible("outertext1"));
        assertFalse(isVisible("innertext1"));
    }

    /**
     * Disabled; the functionality was not well thought out and has been removed; something similar may replace it
     * in the future.
     */
    @Test(enabled = false)
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

    @Test(enabled = false)
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
        assertTextPresent("You must provide a value for Value1.");

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
    public void ajax_form_loop()
    {
        openLinks("FormInjector Demo");

        assertText("sum", "0.0");

        click("link=Add another value");

        waitForAjaxRequestsToComplete();

        type("//input[@type='text'][1]", "5.1");

        clickAndWait(SUBMIT);

        assertText("sum", "5.1");

        click("link=remove");

        waitForAjaxRequestsToComplete();

        clickAndWait(SUBMIT);

        assertText("sum", "0.0");
    }

    @Test
    public void remove_ajaxformloop_values_using_buttons_with_nested_elements()
    {
        openLinks("FormInjector Demo");

        assertText("sum", "0.0");

        click("link=Add another value");

        waitForAjaxRequestsToComplete();

        type("//input[@type='text'][1]", "5.1");

        clickAndWait(SUBMIT);

        assertText("sum", "5.1");

        click("css=.glyphicon-trash");

        waitForAjaxRequestsToComplete();

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

        // Not more more testing can be done; there's no client-side console
        // because Tapestry now favors the native console (when present),
        // and the iframe containing the server-side Ajax exception is opaque.
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

    /**
     * TAP5-2458
     */
    @Test
    public void redirect_from_page_activation_method()
    {
        openLinks("OnActivateRedirect Demo");

        clickAndWait("link");

        assertText("message", "Redirected from XHR");
    }

    /**
     * TAP5-2397
     */
    @Test
    public void add_datefield_in_ajaxformloop()
    {
        openLinks("DateField inside AjaxFormLoop");

        click("link=Add row");

        waitForAjaxRequestsToComplete();

        // now the datefield module is loaded and dom.scanner has been invoked
        // the second insertion is problematic

        click("css=.glyphicon-minus-sign");

        click("link=Add row");

        waitForAjaxRequestsToComplete();

        click("css=.glyphicon-calendar");

        assertTextPresent("Today");

    }
    
    @Test
    public void publishevent() throws InterruptedException
    {
        openLinks("@PublishEvent Demo");
        
        waitForAjaxRequestsToComplete();
        
        final String template = "//table/tbody/tr[%d]/td[%d]";
        
        for (int i = 1; i <= 8; i++) {
            assertEquals(
                    getText(String.format(template, i, 3)),
                    getText(String.format(template, i, 4)),
                    "Row " + i);
        }

//        // An ugly way of giving time for all the AJAX requests to finish
//        // without adding more JavaScript for that.
//        Thread.sleep(3000);
    }
    
}
