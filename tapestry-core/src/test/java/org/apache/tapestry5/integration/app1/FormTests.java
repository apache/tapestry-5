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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tapestry5.corelib.components.Form;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Form} component as well as many form control components.
 */
public class FormTests extends App1TestCase
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

        assertTextSeries("//div[@id='results']//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
        assertFieldValue("t:ac", "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");

        clickAndWait(SUBMIT);

        assertTextSeries("//div[@id='results']//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
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

        assertTextPresent("[foo@bar.baz]");
        assertTextPresent("[Show me the money!]");
        assertTextPresent("[true]");
        assertTextPresent("[19]");
    }

    @Test
    public void client_side_validation()
    {
        openLinks("Client Validation Demo");

        clickAndWait("link=Reset Page State");

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

    // This test has been disabled because the use of the pattern attribute
    // by the Regexp validator, prevents the form from submitting.
    @Test(enabled=false)
    public void regexp_validator()
    {
        openLinks("Regexp Demo");

        String update = SUBMIT;

        type("zipCode", "abc");

        click(update); // but don't wait

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
        openLinks("DateField Demo", "Reset Page State", "english");

        type("birthday", "24 dec 1966");
        type("asteroidImpact", "05/28/2046");

        clickAndWait(SUBMIT);

        assertText("birthday-output", "12/24/1966");
        assertText("impact-output", "05/28/2046");

        assertFieldValue("birthday", "24 Dec 1966");
        assertFieldValue("asteroidImpact", "5/28/2046");

        clickAndWait("link=french");

        click("css=.x-birthday .btn");

        waitForAjaxRequestsToComplete();

        assertText("//A[@class='topLabel']", "1966 d\u00e9cembre");
    }

    // TAP5-2197
    @Test
    public void datefield_leniency()
    {
        openLinks("DateField Demo", "Reset Page State", "english");

        type("asteroidImpact", "00/00/0000");
        type("lenient", "00/00/0000");

        clickAndWait(SUBMIT);

        // By default, DateField is not lenient anymore
        assertText("css=div.x-impact p.help-block", "Date value '00/00/0000' is not parseable.");

        // But this one is configured as such by setting the "lenient" parameter to true.
        assertFalse(isElementPresent("css=div.x-lenient p.help-block"));

        // Check whether a String coerced to a DateFormat results in a lenient or not instance
        // according to the SymbolConstants.LENIENT_DATE_FORMAT symbol (default false)
        assertText("coercedStringToDateFormatLenient", "false");

    }

    // TAP5-1057
    @Test
    public void xss_datefield()
    {
        openLinks("DateField Demo", "Reset Page State", "english");

        type("asteroidImpact", "<script>alert('T5 is great'); </script>");

        click("css=.x-impact .btn");

        waitForAjaxRequestsToComplete();

        assertSourcePresent("Unparseable date: \"&lt;script&gt;alert('T5 is great'); &lt;/script&gt;\"");
    }

    // TAP5-1409
    @Test
    public void datefield_select_newmonth_samedate()
    {
        openLinks("DateField Demo", "Reset Page State", "english");

        //start with a known date...
        type("asteroidImpact", "05/28/2035");

        click("css=.x-impact .btn");

        ExpectedCondition datePickerVisible = ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.datePicker"));

        waitForCondition(datePickerVisible);
        assertEquals(getText("css=td.selected"), "28");

        //move to the next month.
        click("css=button.nextButton");

        //first, make sure that NOTHING shows as selected! The selected date is still 5/28/46

        waitForSelectedToBeRemoved();


        //make sure it's still selected if we navigate back...
        click("css=button.previousButton");
        waitForCssSelectorToAppear("td.selected");

        click("css=button.nextButton");

        waitForSelectedToBeRemoved();

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
        click("css=.x-impact .btn");

        waitForCondition(datePickerVisible);
        click("css=div.datePicker .footerTable button");
        waitForInvisible(pickerGoneSelector);

        String value = getValue("asteroidImpact");
        assertFieldValue("asteroidImpact", new SimpleDateFormat("M/d/yyyy").format(new Date()));

        //#2...
        click("css=.x-impact .btn");

        waitForCondition(datePickerVisible);
        click("css=button.nextButton");

        waitForSelectedToBeRemoved();

        click("css=div.datePicker .footerTable button");
        waitForCssSelectorToAppear("td.selected");

        //#1
        click("css=div.datePicker .footerTable button");
        waitForInvisible(pickerGoneSelector);
        assertFieldValue("asteroidImpact", value);

        //#4...

        click("css=.x-impact .btn");

        waitForCondition(datePickerVisible);

        String noneButton = "//button[text()='None']";

        click(noneButton);

        waitForInvisible(pickerGoneSelector);

        assertFieldValue("asteroidImpact", "");

        click("css=.x-impact .btn");

        waitForCondition(datePickerVisible);
        assertFalse(isElementPresent("css=td.selected"));

        click(noneButton);

        waitForInvisible(pickerGoneSelector);
        assertFieldValue("asteroidImpact", "");
    }

    private void waitForSelectedToBeRemoved()
    {
        waitForCondition("selenium.browserbot.getCurrentWindow().testSupport.findCSSMatchCount('td.selected') == 0", PAGE_LOAD_TIMEOUT);
    }

    // TAP5-1408, TAP5-2203
    @Test
    public void datefield_clickoutside_closes()
    {
        openLinks("DateField Demo", "Reset Page State", "english");

        type("asteroidImpact", "05/28/2046");

        click("css=.x-impact .btn");

        ExpectedCondition datePickerVisible = ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.datePicker"));

        waitForCondition(datePickerVisible);

        click("css=.x-impact .btn");
        waitForInvisible("css=div.datePicker");

        //make sure that clicking somewhere outside the date picker
        //closes it
        click("css=.x-impact .btn");
        waitForCondition(datePickerVisible);

        click("css=h1");
        waitForInvisible("css=div.datePicker");

        //also make sure that clicking the month label /doesn't/ close the picker
        click("css=.x-impact .btn");
        waitForCondition(datePickerVisible);
        click("css=a.topLabel");
        waitForCssSelectorToAppear("div.labelPopup");
        click("css=div.labelPopup a");

        waitForInvisible("css=div.labelPopup");
        //It's basically impossible to express "wait until the popup doesn't disappear"
        //Instead, we take advantage of knowing that the datepicker disappears with this bug /almost/
        //immediately after picking the month label, so we sleep the test for a few seconds to provide
        //ammple time for the bug to manifest.

        waitForAjaxRequestsToComplete();

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

        webDriver.findElements(By.cssSelector("label")).stream().filter(element-> element.getText().contains("Accounting")).findFirst().get().click();
        clickAndWait(update);

        assertTextPresent("Selected department: ACCOUNTING");

        WebElement salesAndMarketing = webDriver.findElements(By.cssSelector("label")).stream().filter(element-> element.getText().contains("Sales And Marketin")).findFirst().get();
        scrollIntoView(salesAndMarketing);
        salesAndMarketing.click();

        clickAndWait(update);

        assertTextPresent("Selected department: SALES_AND_MARKETING");

        // not in a loop ...
        WebElement temp = webDriver.findElements(By.cssSelector("label")).stream().filter(element-> element.getText().contains("Temp")).findFirst().get();
        scrollIntoView(temp);
        temp.click();

        clickAndWait(update);

        assertTextPresent("Selected position: TEMP");

        WebElement lifer = webDriver.findElements(By.cssSelector("label")).stream().filter(element-> element.getText().contains("Lifer")).findFirst().get();
        scrollIntoView(lifer);
        lifer.click();

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

                        "//div[@class='palette']//input[@type='hidden']",

                        "//div[@class='palette-available']//select",

                        "//div[@class='palette-selected']//select",

                        "//input[@name='checklist']",

                        // TAP5-2078
                        "//input[@name='radiogroup']",

                        "//input[@id='submit_0']"};

        for (String path : paths)
        {
            String locator = String.format("%s/@disabled", path);

            assertAttribute(locator, "disabled");
        }

        assertAttribute("css=div.palette .btn@disabled", "true");

        //TAP5-2078
        clickAndWait("//input[@value='Continue']");

        assertFalse(isTextPresent("This should not happen"));
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
        // the field (they do, but that's hard to prove!). It is also susceptible to
        // idiosyncrasies around how Tapestry renders attributes, and how the browser
        // represents them.

        // Along the way we are also testing:
        // - primitive types are automatically required
        // - AbstractTextField.isRequired() and the logic inside
        // ComponentFieldValidator.isRequired()

        assertSourcePresent(
                "[Before label for Value]<label for=\"value\" id=\"value-label\" class=\"control-label\">Value</label>[After label for Value]",
                "[Before field Value]",
                "[After field Value (optional)]",
                "[Before label for Required Value]<label for=\"requiredValue\" id=\"requiredValue-label\" class=\"control-label\">Required Value</label>[After label for Required Value]",
                "[Before field Required Value]", "[After field Required Value (required)]");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void wrapper_types_with_text_field()
    {
        openLinks("TextField Wrapper Types", "Reset Page State");

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

        assertTextPresent("You must provide a numeric value for Amount.",
                "Provide quantity as a number.");
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
        openLinks("Client-Side Numeric Validation", "Reset Page State", "Setup Values");

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

        clickAndWait("link=Switch to German");

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

        clickAndWait("link=Setup Values");

        type("longValue", "4000.");
        click(SUBMIT);

        assertTextPresent("You must provide an integer value for Long Value.");

        type("doubleValue", "abc");

        click(SUBMIT);

        assertTextPresent("You must provide a numeric value for Double Value.");
    }

    @Test
    public void client_validation_for_numeric_fields_that_are_not_required()
    {
        openLinks("Form Zone Demo");

        type("longValue", "alpha");

        click(SUBMIT);

        assertTextPresent("You must provide an integer value for Long Value.");

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

        assertTextPresent("You must provide a value for Name.");
        assertTextPresent("You must provide a value for Age.");

        type("name", "behemoth");
        type("age", "0");
        select("type", "label=Snake");

        click(SUBMIT);
        assertTextPresent("Age requires a value of at least 1.");

        type("age", "121");
        click(SUBMIT);
        assertTextPresent("Age requires a value no larger than 120.");

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

        click("//a[@id='fred']");

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

    /**
     * TAP5-2183
     */
    @Test
    public void link_submit_component_with_nested_element()
    {
        openLinks("LinkSubmit Demo");

        type("name", "Wilma");

        clickAndWait("css=.glyphicon-star");

        assertText("name-value", "Wilma");
        assertText("last-clicked", "Pebbles");
    }

    @Test
    public void calendar_field_inside_bean_editor()
    {
        openLinks("BeanEditor / Calendar Demo", "Reset Page State");

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
    public void create_select_model_from_objects() throws Exception
    {
        openLinks("SelectModel from objects");

        select("track", "label=The Calling");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected track: The Calling, Synaesthetic");
    }

    @Test
    public void create_select_model_coercion() throws Exception
    {
        openLinks("SelectModel coercion");

        waitForElementToAppear("track");
        
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

    /**
     * TAP5-2223.
     */
    @Test
    public void optionGroup_form()
    {
        openLinks("OptionGroupForm Demo");

        assertTextPresent("entity.id: [1]");

        select("entity", "label2");

        clickAndWait(SUBMIT);

        assertTextPresent("entity.id: [2]");
    }

    /** TAP5-2331 */
    @Test
    public void form_fields_client_id_parameter()
    {
        final String[] clientIds = {"clientId-0", "clientId-1"};

        openLinks("Form Field clientId Parameter Demo");

        for (int i = 0; i < 4; i++) {

            for (String clientId : clientIds)
            {
                assertTrue(selenium.isElementPresent(clientId));
            }

            click("updateZone");
            waitForAjaxRequestsToComplete();

        }

    }

    /** TAP5-2301 */
    @Test
    public void select_context() {

        openLinks("MultiZone Update inside a Form");
        selenium.select("selectValue1", "label=3 pre ajax");
        waitForAjaxRequestsToComplete();
        assertEquals(
                "4 post ajax, number 013, retention policy RUNTIME",
                selenium.getText("//select[@id='selectValue2']/option"));

    }

    /** TAP5-1815. In this webapp, HTML5 support is disabled, so we check whether it actually is disabled */
    @Test
    public void html5_support_disabled() throws Exception
    {
        openLinks("ValidForm");
        assertEquals("text", getAttribute("emailValidator@type")); // if HTML5 support was enabled, this would be "email"
    }

    /** TAP5-736 **/
    @Test
    public void textfield_requires_non_null_validate_parameter() throws Exception
    {
        openLinks("TextField with null validate parameter");
        if(isTextPresent("java.lang.NullPointerException")){
            reportAndThrowAssertionError("Unexpected NullPointerException was thrown");
        }
        assertTextPresent("This parameter is not allowed to be null.");
    }

    /** TAP5-2467 **/
    @Test
    public void validate_in_error_event() {
        openLinks("Validate in error Event");

        click(SUBMIT);

        waitForElementToAppear("validate-in-error");

        assertTextPresent("Validate in error");
    }

    /** TAP5-2075 **/
    @Test
    public void validate_checkbox_must_be_checked()
    {
        openLinks("Validate Checkbox Must Be Checked");

        clickAndWait(SUBMIT);

        assertTextPresent("You must check Checkbox.");

        check("//input[@type='checkbox']");

        clickAndWait(SUBMIT);

        assertTextPresent("Checkbox's value: true");
    }

    /** TAP5-2075 **/
    @Test
    public void validate_checkbox_must_be_unchecked()
    {
        openLinks("Validate Checkbox Must Be Unchecked");

        check("//input[@type='checkbox']");

        clickAndWait(SUBMIT);

        assertTextPresent("You must uncheck Checkbox.");

        uncheck("//input[@type='checkbox']");

        clickAndWait(SUBMIT);

        assertTextPresent("Checkbox's value: false");
    }

    // TAP5-2204
    @Test
    public void select_model_with_auto_security_and_non_persistent_model() throws Exception
    {
        openLinks("Select Demo");

        select("month", "label=August");

        clickAndWait(SUBMIT);

        assertTextPresent("Selected month: August");
    }
}
