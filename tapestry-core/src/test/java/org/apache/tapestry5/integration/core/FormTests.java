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

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Form} component as well as many form control components.
 */
public class FormTests extends TapestryCoreTestCase
{

    @Test
    public void form_encoding_type()
    {
        clickThru("Form Encoding Type");

        assertAttribute("//form/@enctype", "x-override");
    }

    @Test
    public void page_context_in_form()
    {
        clickThru("Page Context in Form");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces",
                "context/with/slashes");
        assertFieldValue("t:ac",
                "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");

        clickAndWait(SUBMIT);

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces",
                "context/with/slashes");
        assertFieldValue("t:ac",
                "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");
    }

    @Test
    public void password_field()
    {
        clickThru("PasswordFieldDemo");

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
        clickThru("ValidForm");

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
        clickThru("Client Validation Demo");

        // Used to ensure that the <script> tag was present, but that's hard to
        // do with script combining enabled.

        clickAndWait("link=Clear Data");

        // Notice: click, not click and wait.

        click(SUBMIT);

        // Looks like more weaknesses in Selenium, can only manage the first
        // match not the others.
        assertTextSeries("//div[@class='t-error-popup'][%d]/span", 1,
                "You must provide a value for First Name."
        // , "Everyone has to have a last name!",
        // "Year of Birth requires a value of at least 1900."
        );

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
    public void radio_group_validator()
    {
        clickThru("RadioDemo");

        // Verify that the "required" validator works.
        clickAndWait(SUBMIT);
        assertTextPresent("You must provide a value for Department.");
    }

    @Test
    public void regexp_validator()
    {
        clickThru("Regexp Demo");

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
        clickThru("DateField Demo", "clear", "english");

        type("birthday", "24 dec 1966");
        type("asteroidImpact", "05/28/2046");

        clickAndWait(SUBMIT);

        assertTextPresent("Birthday: [12/24/1966]");
        assertTextPresent("Impact: [05/28/2046]");

        assertFieldValue("birthday", "24 Dec 1966");
        assertFieldValue("asteroidImpact", "5/28/2046");

        clickAndWait("link=french");

        click("birthday-trigger");

        waitForCondition(
                "selenium.browserbot.getCurrentWindow().$$('DIV.datePicker').first().isDeepVisible() == true",
                PAGE_LOAD_TIMEOUT);

        assertText("//A[@class='topLabel']", "1966 d\u00e9cembre");

        clickAndWait("link=english");
    }

    @Test
    public void event_based_translate() throws Exception
    {
        clickThru("EventMethod Translator");

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
        clickThru("RadioDemo");

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
        clickThru("Disabled Fields");

        String[] paths = new String[]
        { "//input[@id='textfield']",

        "//input[@id='passwordfield']",

        "//textarea[@id='textarea']",

        "//input[@id='checkbox']",

        "//select[@id='select']",

        "//input[@id='radio1']",

        "//input[@id='radio2']",

        "//input[@id='datefield']",

        "//select[@id='palette-avail']",

        "//button[@id='palette-select']",

        "//button[@id='palette-deselect']",

        "//select[@id='palette']",

        "//input[@id='submit']" };

        for (String path : paths)
        {
            String locator = String.format("%s/@disabled", path);

            assertAttribute(locator, "disabled");
        }
    }

    /**
     * TAPESTRY-2056
     */
    @Test
    public void null_field_strategy()
    {
        clickThru("Null Field Strategy Demo");

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
        clickThru("Override Validation Decorator");

        // This is sub-optimal, as it doesn't esnure that the before/after field
        // values really do wrap around
        // the field (they do, but that's hard to prove!).

        // Along the way we are also testing:
        // - primitive types are automatically required
        // - AbstractTextField.isRequired() and the logic inside
        // ComponentFieldValidator.isRequired()

        assertSourcePresent(
                "[Before label for Value]<label id=\"value-label\" for=\"value\">Value</label>[After label for Value]",
                "[Before field Value]",
                "[After field Value (optional)]",
                "[Before label for Required Value]<label id=\"requiredValue-label\" for=\"requiredValue\">Required Value</label>[After label for Required Value]",
                "[Before field Required Value]", "[After field Required Value (required)]");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void wrapper_types_with_text_field()
    {
        clickThru("TextField Wrapper Types", "clear");

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
        clickThru("Submit With Context");

        clickAndWait(SUBMIT);

        assertTextPresent("Result: 10.14159");
    }
}
