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
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Tests for the {@link Form} component as well as many form control components.
 */
public class FormTests extends TapestryCoreTestCase
{

    @Test
    public void form_encoding_type()
    {
        openLinks("Form Encoding Type");

        assertAttribute("//form/@enctype", "x-override");
    }

    @Test
    public void page_context_in_form()
    {
        openLinks("Page Context in Form");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
        assertFieldValue("t:ac", "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");

        clickAndWait(SUBMIT);

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
        assertFieldValue("t:ac", "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");
    }

    @Test
    public void password_field()
    {
        openLinks("PasswordFieldDemo");

        type("userName", "howard");
        type("password", "wrong-password");

        clickAndWait(SUBMIT);

        assertFieldValue("userName", "howard");
        // Verify that password fields do not render a non-blank password, even
        // when it is known.
        assertFieldValue("password", "");

        assertTextPresent("[howard]");
        assertTextPresent("[wrong-password]");

        type("password", "tapestry");

        clickAndWait(SUBMIT);

        assertTextPresent("You have provided the correct user name and password.");
    }

    @Test
    public void server_side_validation_for_textfield_and_textarea() throws Exception
    {
        openLinks("ValidForm");

        clickAndWait(SUBMIT);
        assertTextPresent("You must provide a value for Email.");
        // is an overridden validation error message:
        assertTextPresent("Please provide a detailed description of the incident.");

        // Check on decorations via the default validation decorator:

        assertAttribute("//label[1]/@class", "t-error");
        assertAttribute("//label[2]/@class", "t-error");
        assertAttribute("//input[@id='email']/@class", "t-error");
        assertAttribute("//textarea[@id='message']/@class", "t-error");

        type("email", "foo@bar.baz");
        type("message", "Show me the money!");
        type("hours", "foo");

        clickAndWait(SUBMIT);

        assertTextPresent("[false]");
        assertTextPresent("You must provide an integer value for Hours.");

        assertAttribute("//input[@id='hours']/@value", "foo");

        type("hours", " 19 ");
        click("//input[@id='urgent']");
        clickAndWait(SUBMIT);

        // Make sure the decoration went away.

        // Sorry, not sure how to do that, since the attributes don't exist, we
        // get xpath errors.

        // assertText("//label[1]/@class", "");
        // assertText("//label[2]/@class", "");
        // assertText("//input[@id='email']/@class", "");
        // assertText("//textarea[@id='message']/@class", "");

        assertTextPresent("[foo@bar.baz]");
        assertTextPresent("[Show me the money!]");
        assertTextPresent("[true]");
        assertTextPresent("[19]");
    }

    @Test
    public void client_side_validation()
    {
        openLinks("Client Validation Demo");

        // Used to ensure that the <script> tag was present, but that's hard to
        // do with script combining enabled.

        clickAndWait("link=Clear Data");

        // Notice: click, not click and wait.

        click(SUBMIT);

        assertTextPresent("You must provide a value for First Name.");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1000");
        type("password", "supersecret");

        click(SUBMIT);

        type("birthYear", "1966");
        click("citizen");

        clickAndWait(SUBMIT);

        assertTextPresent("Howard", "Lewis Ship", "1966", "U.S. Citizen");
    }

    @Test
    public void cancel_button()
    {
        openLinks("Client Validation Demo");

        // Used to ensure that the <script> tag was present, but that's hard to
        // do with script combining enabled.

        clickAndWait("link=Clear Data");

        clickAndWait("//input[@value='Cancel']");

        assertText("message", "Form was cancelled.");
    }

    @Test
    public void radio_group_validator()
    {
        openLinks("RadioDemo", "reset");

        // Verify that the "required" validator works.
        clickAndWait(SUBMIT);

        assertTextPresent("You must provide a value for Department.");
    }

    @Test
    public void regexp_validator()
    {
        openLinks("Regexp Demo");

        String update = SUBMIT;

        type("zipCode", "abc");

        click(update); // but don't wait

        waitForCondition("selenium.browserbot.getCurrentWindow().document.getElementById('zipCode_errorpopup')", "5000");

        assertTextPresent("A zip code consists of five or nine digits, eg: 02134 or 90125-4472.");

        type("zipCode", "12345");

        clickAndWait(update);

        assertTextPresent("Zip code: [12345]");

        type("zipCode", "12345-9876");

        clickAndWait(update);

        assertTextPresent("Zip code: [12345-9876]");
    }

    @Test
    public void basic_datefield()
    {
        openLinks("DateField Demo", "clear", "english");

        type("birthday", "24 dec 1966");
        type("asteroidImpact", "05/28/2046");

        clickAndWait(SUBMIT);

        assertTextPresent("Birthday: [12/24/1966]");
        assertTextPresent("Impact: [05/28/2046]");

        assertFieldValue("birthday", "24 Dec 1966");
        assertFieldValue("asteroidImpact", "5/28/2046");

        clickAndWait("link=french");

        click("birthday-trigger");

        waitForCondition("selenium.browserbot.getCurrentWindow().$$('DIV.datePicker').first().isDeepVisible() == true",
                PAGE_LOAD_TIMEOUT);

        assertText("//A[@class='topLabel']", "1966 d\u00e9cembre");

        clickAndWait("link=english");
    }

    // TAP5-1057
    @Test
    public void xss_datefield()
    {
        openLinks("DateField Demo", "clear", "english");

        type("asteroidImpact", "<script>alert('T5 is great'); </script>");

        click("id=asteroidImpact-trigger");

        assertBubbleMessage("asteroidImpact", "Unparseable date: \"&lt;script&gt;alert('T5 is great'); &lt;/script&gt;\"");
    }

    // TAP5-1409
    @Test
    public void datefield_select_newmonth_samedate()
    {
        openLinks("DateField Demo", "clear", "english");
        //start with a known date...
        type("asteroidImpact", "05/28/2035");

        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        assertEquals(getText("css=td.selected"), "28");

        //move to the next month.
        click("css=button.nextButton");
        //first, make sure that NOTHING shows as selected! The selected date is still 5/28/46
        String selectedGoneCondition = "window.$$(\"td.selected\").size() == 0";
        waitForCondition(selectedGoneCondition, PAGE_LOAD_TIMEOUT);

        //make sure it's still selected if we navigate back...
        click("css=button.previousButton");
        waitForCSSSelectedElementToAppear("td.selected");

        click("css=button.nextButton");
        waitForCondition(selectedGoneCondition, PAGE_LOAD_TIMEOUT);

        click("xpath=//td[text()='28']");
        String pickerGoneSelector = "css=div.datePicker";
        waitForInvisible(pickerGoneSelector);

        assertFieldValue("asteroidImpact", "6/28/2035");

        //a few other behaviors to check on as a side-effect of implementing the fix for 1409:
        //1) If today is selected and it's the current month, pressing the "Today" button should close the popup
        //2) If today is selected and we're on some other month, pressing the "Today" button should just take us
        //   back to the today.
        //3) If today is not selected, pressing the "Today" button should set the date and close the popup.
        //4) Pressing the "None" button should always close the popup and result in no date.

        //#3
        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        click("css=button.todayButton");
        waitForInvisible(pickerGoneSelector);

        String value = getValue("asteroidImpact");
        assertFieldValue("asteroidImpact", new SimpleDateFormat("M/d/yyyy").format(new Date()));

        //#2...
        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        click("css=button.nextButton");
        waitForCondition(selectedGoneCondition, PAGE_LOAD_TIMEOUT);

        click("css=button.todayButton");
        waitForCSSSelectedElementToAppear("td.selected");

        //#1
        click("css=button.todayButton");
        waitForInvisible(pickerGoneSelector);
        assertFieldValue("asteroidImpact", value);

        //#4...
        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        click("css=button.noneButton");
        waitForInvisible(pickerGoneSelector);
        assertFieldValue("asteroidImpact", "");

        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        assertFalse(isElementPresent("css=td.selected"));
        click("css=button.noneButton");
        waitForInvisible(pickerGoneSelector);
        assertFieldValue("asteroidImpact", "");
    }

    // TAP4-1408
    @Test
    public void datefield_clickoutside_closes()
    {
        openLinks("DateField Demo", "clear", "english");
        type("asteroidImpact", "05/28/2046");

        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");

        click("id=asteroidImpact");
        waitForInvisible("css=div.datePicker");

        //also make sure that clicking the month label /doesn't/ close the picker
        click("id=asteroidImpact-trigger");
        waitForCSSSelectedElementToAppear("div.datePicker");
        click("css=a.topLabel");
        waitForCSSSelectedElementToAppear("div.labelPopup");
        click("css=div.labelPopup a");

        waitForCondition("!selenium.isElementPresent('css=div.labelPopup')", PAGE_LOAD_TIMEOUT);
        //It's basically impossible to express "wait until the popup doesn't disappear" 
        //Instead, we take advantage of knowing that the datepicker disappears with this bug /almost/ 
        //immediately after picking the month label, so we sleep the test for a few seconds to provide
        //ammple time for the bug to manifest. 
        try {
            Thread.sleep(1500);
        } catch (Exception e){/*Ignore the interrupted exception */}
        assertTrue(isVisible("css=div.datePicker"));
    }

    @Test
    public void event_based_translate() throws Exception
    {
        openLinks("EventMethod Translator");

        type("count", "123");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [123]");

        type("count", "0");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [0]");

        assertFieldValue("count", "zero");

        type("count", "456");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [456]");

        assertFieldValue("count", "456");

        type("count", "ZERO");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [0]");

        assertFieldValue("count", "zero");

        // Try the server-side custom exception reporting.

        type("count", "13");
        clickAndWait(SUBMIT);

        assertTextPresent("Event Handler Method Translate", "Thirteen is an unlucky number.");

        type("count", "i");
        clickAndWait(SUBMIT);

        assertTextPresent("Event Handler Method Translate", "Rational numbers only, please.");
    }

    @Test
    public void radio_button_and_group()
    {
        openLinks("RadioDemo");

        String update = SUBMIT;

        // in a loop ...
        click("//label[.='Accounting']");
        clickAndWait(update);
        assertTextPresent("Selected department: ACCOUNTING");

        click("//label[.='Sales And Marketing']");
        clickAndWait(update);
        assertTextPresent("Selected department: SALES_AND_MARKETING");

        // not in a loop ...
        click("//label[.='Temp']");
        clickAndWait(update);
        assertTextPresent("Selected position: TEMP");

        click("//label[.='Lifer']");
        clickAndWait(update);
        assertTextPresent("Selected position: LIFER");
    }

    @Test
    public void disabled_fields() throws Exception
    {
        openLinks("Disabled Fields");

        // The couple of places where there's a _0 suffix is related to
        // the fix for https://issues.apache.org/jira/browse/TAP5-1632
        String[] paths = new String[]
                {"//input[@id='textfield']",

                        "//input[@id='passwordfield']",

                        "//textarea[@id='textarea']",

                        "//input[@id='checkbox']",

                        "//select[@id='select_0']",

                        "//input[@id='radio1']",

                        "//input[@id='radio2']",

                        "//input[@id='datefield']",

                        "//select[@id='palette-avail']",

                        "//button[@id='palette-select']",

                        "//button[@id='palette-deselect']",

                        "//select[@id='palette']",

                        "//input[@id='submit_0']"};

        for (String path : paths)
        {
            String locator = String.format("%s/@disabled", path);

            assertAttribute(locator, "true");
        }
    }

    /**
     * TAPESTRY-2056
     */
    @Test
    public void null_field_strategy()
    {
        openLinks("Null Field Strategy Demo");

        String locator = "//span[@id='value']";

        assertText(locator, "");

        assertAttribute("//input[@id='number']/@value", "0");

        type("number", "");

        clickAndWait(SUBMIT);

        assertText(locator, "0");
    }

    /**
     * TAPESTRY-1647
     */
    @Test
    public void label_invokes_validation_decorator_at_correct_time()
    {
        openLinks("Override Validation Decorator");

        // This is sub-optimal, as it doesn't esnure that the before/after field
        // values really do wrap around
        // the field (they do, but that's hard to prove!).

        // Along the way we are also testing:
        // - primitive types are automatically required
        // - AbstractTextField.isRequired() and the logic inside
        // ComponentFieldValidator.isRequired()

        assertSourcePresent(
                "[Before label for Value]<label for=\"value\">Value</label>[After label for Value]",
                "[Before field Value]",
                "[After field Value (optional)]",
                "[Before label for Required Value]<label for=\"requiredValue\">Required Value</label>[After label for Required Value]",
                "[Before field Required Value]", "[After field Required Value (required)]");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void wrapper_types_with_text_field()
    {
        openLinks("TextField Wrapper Types", "clear");

        assertFieldValue("count", "");
        assertText("value", "null");

        type("count", "0");
        clickAndWait(SUBMIT);

        assertFieldValue("count", "0");
        assertText("value", "0");

        type("count", "1");
        clickAndWait(SUBMIT);

        assertFieldValue("count", "1");
        assertText("value", "1");

        clickAndWait("link=clear");

        assertFieldValue("count", "");
        assertText("value", "null");
    }

    @Test
    public void submit_with_context()
    {
        openLinks("Submit With Context");

        clickAndWait(SUBMIT);

        assertTextPresent("Result: 10.14159");
    }

    /**
     * TAPESTRY-2563
     */
    @Test
    public void form_action_via_get()
    {
        open(getBaseURL() + "validform.form", "true");

        assertTextPresent("Forms require that the request method be POST and that the t:formdata query parameter have values.");
    }

    /**
     * TAPESTRY-2352
     */
    @Test
    public void client_field_format_validation()
    {
        openLinks("Client Format Validation");

        type("amount", "abc");
        type("quantity", "abc");

        click(SUBMIT);

        waitForElementToAppear("amount_errorpopup");
        waitForElementToAppear("quantity_errorpopup");

        assertText("//div[@id='amount_errorpopup']/span", "You must provide a numeric value for Amount.");
        assertText("//div[@id='quantity_errorpopup']/span", "Provide quantity as a number.");
    }

    /**
     * TAPESTRY-2438
     */
    @Test
    public void validation_exception_thrown_from_validate_form_event_handler()
    {
        openLinks("ValidationForm ValidationException Demo");

        clickAndWait(SUBMIT);

        assertTextPresent("From event handler method.");

        assertText("event", "failure");
    }

    @Test
    public void form_field_outside_form()
    {
        openLinks("Form Field Outside Form");

        assertTextPresent(
                "org.apache.tapestry5.internal.services.RenderQueueException",
                "Render queue error in SetupRender[FormFieldOutsideForm:textfield]: Component FormFieldOutsideForm:textfield must be enclosed by a Form component.",
                "context:FormFieldOutsideForm.tml, line 5");
    }

    /**
     * TAP5-281
     */
    @Test
    public void nested_form_check()
    {
        openLinks("Nested Form Demo");

        assertTextPresent("Form components may not be placed inside other Form components.");
    }

    /**
     * TAP5-87
     */
    @Test
    public void blank_password_does_not_update()
    {
        openLinks("Blank Password Demo");

        type("password", "secret");

        clickAndWait(SUBMIT);

        assertFieldValue("password", "");

        assertText("visiblepassword", "secret");

        clickAndWait(SUBMIT);

        assertFieldValue("password", "");

        assertText("visiblepassword", "secret");
    }

    /**
     * TAP5-228: And to think I almost blew off the integration tests!
     */
    @Test
    public void per_form_validation_messages_and_constraints()
    {
        openLinks("Per-Form Validation Messages");

        clickAndWait("//input[@type='submit' and @value='Login']");

        assertTextPresent("Enter the unique user id you provided when you registerred.");

        type("userId", "aaa");

        clickAndWait("//input[@type='submit' and @value='Login']");

        assertTextPresent("You must provide at least 10 characters for User Id.");

        clickAndWait("//input[@type='submit' and @value='Register']");

        assertTextPresent("Enter a unique user id, such as your initials.");

        type("userId_0", "aaa");

        clickAndWait("//input[@type='submit' and @value='Register']");

        assertTextPresent("You must provide at least 20 characters for User Id.");
    }

    /**
     * TAP5-719
     */
    @Test
    public void link_submit_without_validator()
    {
        openLinks("LinkSubmit Without Validator Demo");

        type("searchField", "Anders Haraldsson");

        clickAndWait("//a[@id='searchLink']");

        assertTextPresent("Result: Anders Haraldsson not found!");
    }

    /**
     * TAP5-211
     */
    @Test
    public void client_side_numeric_validation()
    {
        openLinks("Client-Side Numeric Validation", "reset");

        assertText("outputLongValue", "1000");
        assertText("outputDoubleValue", "1234.67");

        assertFieldValue("longValue", "1000");
        assertFieldValue("doubleValue", "1,234.67");

        type("longValue", "2,000 ");
        type("doubleValue", " -456,789.12");

        clickAndWait(SUBMIT);

        assertText("outputLongValue", "2000");
        assertText("outputDoubleValue", "-456789.12");

        assertFieldValue("longValue", "2000");
        assertFieldValue("doubleValue", "-456,789.12");

        clickAndWait("link=switch to German");

        assertText("outputLongValue", "2000");
        assertText("outputDoubleValue", "-456789.12");

        assertFieldValue("longValue", "2000");
        assertFieldValue("doubleValue", "-456.789,12");

        type("longValue", "3.000");
        type("doubleValue", "5.444.333,22");

        clickAndWait(SUBMIT);

        assertFieldValue("longValue", "3000");
        assertFieldValue("doubleValue", "5.444.333,22");

        assertText("outputLongValue", "3000");
        assertText("outputDoubleValue", "5444333.22");

        clickAndWait("link=reset");

        type("longValue", "4000.");
        click(SUBMIT);

        assertBubbleMessage("longValue", "You must provide an integer value for Long Value.");

        type("doubleValue", "abc");

        click(SUBMIT);

        assertBubbleMessage("doubleValue", "You must provide a numeric value for Double Value.");
    }

    @Test
    public void client_validation_for_numeric_fields_that_are_not_required()
    {
        openLinks("Form Zone Demo");

        type("longValue", "alpha");

        click(SUBMIT);

        waitForElementToAppear("longValue_errorpopup");

        assertText("//div[@id='longValue_errorpopup']/span", "You must provide an integer value for Long Value.");

        type("longValue", "37");

        click(SUBMIT);

        waitForElementToAppear("outputvalue");

        assertText("outputvalue", "37");
    }

    @Test
    public void hidden_field()
    {
        openLinks("Hidden Demo", "setup");

        clickAndWait(SUBMIT);

        assertText("stored", "12345");
    }

    @Test
    public void validation_constraints_from_messages()
    {
        openLinks("Validation Constraints From Messages");

        click(SUBMIT);

        assertBubbleMessage("name", "You must provide a value for Name.");
        assertBubbleMessage("age", "You must provide a value for Age.");

        type("name", "behemoth");
        type("age", "0");
        select("type", "label=Snake");

        click(SUBMIT);
        assertBubbleMessage("age", "Age requires a value of at least 1.");

        type("age", "121");
        click(SUBMIT);
        assertBubbleMessage("age", "Age requires a value no larger than 120.");

        type("age", "5");
        clickAndWait(SUBMIT);
    }

    /**
     * TAP5-157
     */
    @Test
    public void link_submit_component()
    {
        openLinks("LinkSubmit Demo");

        // Wait a moment for the page to initialize.

        waitForElementToAppear("fred");

        click("//a[@id='fred']");

        waitForElementToAppear("name_errorpopup");

        assertTextPresent("You must provide a value for Name.");

        type("name", "Wilma");

        clickAndWait("link=Fred");

        assertText("name-value", "Wilma");
        assertText("last-clicked", "Fred");
        assertTextPresent("Result: 10.14159");

        type("name", "Betty");
        clickAndWait("link=Barney");

        assertText("name-value", "Betty");
        assertText("last-clicked", "Barney");
    }

    @Test
    public void calendar_field_inside_bean_editor()
    {
        openLinks("BeanEditor / Calendar Demo", "clear");

        type("calendar", "04/06/1978");

        clickAndWait(SUBMIT);

        assertTextPresent("Apr 6, 1978");

        // TAP5-1043
        clickAndWait("link=clear");
    }

    @Test
    public void image_submit_triggers_selected_event()
    {
        openLinks("Submit with an Image Demo");

        type("value", "barney gumble");

        clickAndWait("//input[@type='image']");

        assertText("outputvalue", "barney gumble");

        assertText("eventfired", "true");
    }

    /**
     * Tests for forms and form submissions and basic form control components.
     * also tests a few other things, such as
     * computed default bindings and invisible instrumentation.
     */
    @Test
    public void simple_form()
    {
        openLinks("SimpleForm");

        assertText("//label[@for='disabled']", "Disabled");

        // This demonstrates TAPESTRY-1642:
        assertText("//label[@for='email']", "User Email");

        assertText("//label[@for='message']", "Incident Message");
        assertText("//label[@for='operatingSystem']", "Operating System");
        assertText("//label[@for='department']", "Department");
        assertText("//label[@for='urgent']", "Urgent Processing Requested");

        assertFieldValue("email", "");
        assertFieldValue("message", "");
        assertFieldValue("operatingSystem", "osx");
        assertFieldValue("department", "");
        assertFieldValue("urgent", "on");

        clickAndWait(SUBMIT);

        assertTextPresent("department: []");

        type("email", "foo@bar.baz");
        type("message", "Message for you, sir!");
        select("operatingSystem", "Windows NT");
        select("department", "R&D");
        click("urgent");

        clickAndWait(SUBMIT);

        assertFieldValue("email", "foo@bar.baz");
        assertFieldValue("message", "Message for you, sir!");
        assertFieldValue("urgent", "off");

        // Tried to use "email:" and "exact:email:" but Selenium 0.8.1 doesn't
        // seem to accept that.

        assertTextPresent("[foo@bar.baz]", "[Message for you, sir!]", "[false]", "[winnt]", "[RESEARCH_AND_DESIGN]");

        // Haven't figured out how to get selenium to check that fields are
        // disabled.
    }

    /**
     * TAP5-915
     */
    @Test
    public void override_datefield_message_catalog()
    {
        open(getBaseURL() + "overridedatefieldmessagecatalogdemo");

        type("birthday", "aaaaa");

        clickAndWait("//input[@type='submit' and @value='Go']");

        assertTextPresent("The input 'aaaaa' is not a valid date");
    }

    /**
     * TAP5-52.
     */
    @Test
    public void single_error_message()
    {
        open(getBaseURL() + "singleerrordemo");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide a value for Username");
        assertTextPresent("You must provide a value for Password");

        type("username", "Igor");

        clickAndWait(SUBMIT);

        assertFalse(isTextPresent("You must provide a value for Username"));
        assertTextPresent("You must provide a value for Password");

        type("password", "secret");

        clickAndWait(SUBMIT);

        assertFalse(isTextPresent("You must provide a value for Username"));
        assertFalse(isTextPresent("You must provide a value for Password"));
    }

    /**
     * TAP5-1024
     */
    @Test
    public void use_of_cancel_mode_on_submit_button()
    {
        openLinks("Cancel Demo");

        clickAndWait("//input[@value='Cancel Form']");

        assertText("message", "Form was canceled.");
    }

    @Test
    public void use_of_cancel_mode_with_submitlink()
    {
        openLinks("Cancel Demo");

        clickAndWait("link=Cancel Form");

        assertText("message", "Form was canceled.");
    }

    @Test
    public void validation_decoration_for_select() throws Exception
    {
        openLinks("Select Demo");

        clickAndWait(SUBMIT);
        assertTextPresent("You must provide a value for Color.");

        // Check on decorations via the default validation decorator:

        assertAttribute("//label[@for='color']/@class", "t-error");
        assertAttribute("//select[@id='color']/@class", "t-error");
        assertAttribute("//img[@id='color_icon']/@class", "t-error-icon");

        select("color", "label=Green");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected color: Green");
    }

    /**
     * TAP5-1098.
     */
    @Test
    public void create_select_model_from_objects_and_property_name() throws Exception
    {
        openLinks("SelectModel from objects and property name");

        select("track", "label=The Calling");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected track: The Calling, Synaesthetic");
    }

    @Test
    public void validation_macro() throws Exception
    {
        openLinks("Validator Macro Demo");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide a value for Password.");
        assertTextPresent("You must provide a value for Password2.");

        type("password", "abcdefg");
        type("password2", "x");

        clickAndWait(SUBMIT);

        assertTextPresent("You may provide at most 3 characters for Password.");
        assertTextPresent("You must provide at least 2 characters for Password2.");

        type("password", "a");
        type("password2", "wxyz");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide at least 2 characters for Password.");
        assertTextPresent("You may provide at most 3 characters for Password2.");

        type("password", "ab");
        type("password2", "xyz");

        clickAndWait(SUBMIT);

        assertTextPresent("Password: ab");
        assertTextPresent("Password2: xyz");
    }

    @Test
    public void checklist_select() throws Exception
    {
        openLinks("Checklist Demo");

        clickAndWait(SUBMIT);
        assertTextPresent("You must provide a value for Color.");

        check("//input[@value='Green']");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected colors: [Green]");

        check("//input[@value='Red']");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected colors: [Green, Red]");

        check("//input[@value='Blue']");
        uncheck("//input[@value='Green']");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected colors: [Blue, Red]");
    }
    
    @Test
    public void checkFormLinkParameters() throws Exception
    {
        openLinks("FormLinkParameters Demo");
        assertAttribute("//input[@name='myparam']/@value", "!@#$%^&*()_+=");

        clickAndWait("link=SetValue");
        assertTextPresent("Result = '!@#$%^&*()_+='");
        
        clickAndWait(SUBMIT);
        assertTextPresent("Result = '!@#$%^&*()_+='");
    }
}
