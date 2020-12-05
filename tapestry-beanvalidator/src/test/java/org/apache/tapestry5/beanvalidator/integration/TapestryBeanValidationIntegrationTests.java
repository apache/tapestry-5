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
package org.apache.tapestry5.beanvalidator.integration;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@Test(sequential = true, groups = "integration")
@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class TapestryBeanValidationIntegrationTests extends SeleniumTestCase
{
    public static final String AVAILABLE_OPTIONS = "css=.palette-available select";

    public static final String SELECT_BUTTON = "css=.palette [data-action=select]";

    @Test
    public void form_validation() throws Exception
    {
        openLinks("Form Validation Demo");

        //Test JSR-303 validator

        clickAndWait(SUBMIT);

        assertTextPresent("Login Name may not be null");
        assertTextPresent("Secret Password may not be null");
        assertTextPresent("Programming Languages size must be between 2 and 3");
        assertTextPresent("Favorite Colors may not be null");
        assertTextPresent("More Colors size must be between 3 and 4");
        assertTextPresent("Birth Day may not be null");
        assertTextPresent("Accepted Terms And Conditions must be true");
        assertTextPresent("Subscribed must be false");


        type("secretPassword", "igor");

        addSelection(AVAILABLE_OPTIONS, "label=Java");
        addSelection(AVAILABLE_OPTIONS, "label=Ruby");
        click(SELECT_BUTTON);

        select("favoriteColors", "label=Green");

        type("birthDay", "01.01.5000");

        clickAndWait(SUBMIT);

        assertTextPresent("Login Name may not be null");
        assertFalse(isTextPresent("Secret Password may not be null"));
        assertFalse(isTextPresent("Programming Languages size must be between 2 and 3"));
        assertFalse(isTextPresent("Favorite Colors may not be null"));
        assertTextPresent("More Colors size must be between 3 and 4");
        assertTextPresent("Birth Day must be in the past");
        assertTextPresent("Accepted Terms And Conditions must be true");
        assertTextPresent("Subscribed must be false");

        //Test Tapestry validator

        type("loginName", "igor");
        type("birthDay", "6.04.1978");

        check("//input[@value='White']");
        check("//input[@value='Yellow']");
        check("//input[@value='Orange']");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide at least 5 characters for Login Name.");
        assertFalse(isTextPresent("Birth Day must be in the past"));
        assertFalse(isTextPresent("More Colors size must be between 3 and 4"));
        assertTextPresent("Accepted Terms And Conditions must be true");
        assertTextPresent("Subscribed must be false");

        type("loginName", "igor123");

        clickAndWait(SUBMIT);

        assertFalse(isTextPresent("You must provide at least 5 characters for Login Name."));
        assertTextPresent("Accepted Terms And Conditions must be true");
        assertTextPresent("Subscribed must be false");

        click("acceptedTermsAndConditions");

        clickAndWait(SUBMIT);

        assertFalse(isTextPresent("Accepted Terms And Conditions must be true"));
        assertTextPresent("Subscribed must be false");

        click("subscribed");

        clickAndWait(SUBMIT);

        assertFalse(isTextPresent("Subscribed must be false"));
    }

    @Test
    public void beaneditform_validation() throws Exception
    {
        openLinks("BeanEditForm Validation Demo");
        
        clickAndWait(SUBMIT);

        assertTextPresent("User Name may not be null", "Password may not be null");

        type("userName", "igor");

        clickAndWait(SUBMIT);

        assertTextPresent("User Name size must be between 7 and 10", "User Name must match \"[0-9]+\"");
    }

    @Test
    public void inject_validator() throws Exception
    {
        openLinks("Inject Validator Demo");

        clickAndWait(SUBMIT);

        assertTextPresent("User Name may not be null");
    }

    @Test
    public void client_validation() throws Exception
    {
        openLinks("Client Validation Demo");

        //@NotNull
        click(SUBMIT);

        waitForCssSelectorToAppear("p[data-error-block-for='notNullValue']");
        assertTextPresent("Not Null Value may not be null");

        type("notNullValue", "igor");

        //@Min
        type("minValue", "3");

        click(SUBMIT);

        waitForCssSelectorToAppear("p[data-error-block-for='minValue']");
        assertTextPresent("Min Value must be greater than or equal to 6");

        //@Max
        type("minValue", "6");
        type("maxValue", "123");

        click(SUBMIT);

        assertTextPresent("Max Value must be less than or equal to 100");

        //@Null
        type("maxValue", "100");
        type("nullValue", "igor");

        //@Size(min,max)
        type("stringSizeValue", "a");

        click(SUBMIT);

        waitForCssSelectorToAppear("p[data-error-block-for='stringSizeValue']");
        assertTextPresent("String Size Value size must be between 3 and 6");
        
        //@Size(min) TAP5-2158
        type("stringMinLength", "a");

        click(SUBMIT);

        final String stringMinLengthErrorMessage = "String Min Length size must be between 3 and " + Integer.MAX_VALUE;
        
        waitForCssSelectorToAppear("p[data-error-block-for='stringMinLength']");
		assertTextPresent(stringMinLengthErrorMessage);
        
        type("stringMinLength", "aaaaaa");

        click(SUBMIT);

        waitForInvisible("p[data-error-block-for='stringMinLength']");
        assertFalse(isTextPresent(stringMinLengthErrorMessage));
        
        //@Size(max) TAP5-2158
        type("stringMaxLength", "aaaaaaaaaaaaaaaaaaaaaaaaaa");

        click(SUBMIT);

        final String stringMaxLengthErrorMessage = "String Max Length size must be between 0 and 6";
        
        waitForCssSelectorToAppear("p[data-error-block-for='stringMaxLength']");
		assertTextPresent(stringMaxLengthErrorMessage);

        type("stringMaxLength", "aaaaa");

        click(SUBMIT);

        waitForInvisible("p[data-error-block-for='stringMaxLength']");
        assertFalse(isTextPresent(stringMaxLengthErrorMessage));

        click(SUBMIT);

        type("stringSizeValue", "ab");

        click(SUBMIT);

        // Have to select at least one value

        addSelection(AVAILABLE_OPTIONS, "label=Ruby");
        click(SELECT_BUTTON);

        click(SUBMIT);

        waitForCssSelectorToAppear("p[data-error-block-for='languages']");
        assertTextPresent("Languages size must be between 2 and 3");

        waitForCssSelectorToAppear("p[data-error-block-for='nullValue']");
        assertTextPresent("Null Value must be null");

        waitForCssSelectorToAppear("p[data-error-block-for='mustBeTrue']");
        assertTextPresent("Must Be True must be true");
        waitForCssSelectorToAppear("p[data-error-block-for='mustBeFalse']");
        assertTextPresent("Must Be False must be false");

        click("mustBeTrue");

        click(SUBMIT);

        waitForInvisible("p[data-error-block-for='mustBeTrue']");
        // assertFalse(isTextPresent("Must Be True must be true"));
        assertTextPresent("Must Be False must be false");

        click("mustBeFalse");

        click(SUBMIT);

        waitForInvisible("p[data-error-block-for='mustBeFalse']");
        // assertFalse(isTextPresent("Must Be False must be false"));

    }


    @Test
    public void form_client_validation() throws Exception
    {
        openLinks("Form Client Validation Demo");

        click(SUBMIT);

        assertTextPresent("Login Name may not be null");
        assertTextPresent("Secret Password may not be null");
        assertTextPresent("Favorite Colors may not be null");
        assertTextPresent("Birth Day may not be null");


        type("loginName", "123456");
        click(SUBMIT);

        assertTextPresent("Login Name must match \"[a-zA-Z]+\"");

        type("loginName", "abc");
        click(SUBMIT);

        assertTextPresent("You must provide at least 5 characters for Login Name.");
    }

    /*
    * Ensures TAP5-1393 is fixed.
    */
    @Test
    public void form_on_prepare() throws Exception
    {
        openLinks("OnPrepare Demo");

        clickAndWait(SUBMIT);

        assertTextPresent("Login Name may not be null", "Secret Password may not be null");

        type("loginName", "igor");

        clickAndWait(SUBMIT);

        assertTextPresent("Login Name size must be between 7 and 10", "Login Name must match \"[0-9]+\"");
    }

    @Test
    public void beaneditor_validation() throws Exception
    {
        openLinks("ComplexBean Demo");

        // Test JSR-303 validator

        clickAndWait(SUBMIT);

        assertTextPresent("Simple Not Null Property may not be null",
                "Min Value must be greater than or equal to 6", "Not Null String may not be null");
    }

    // TAP5-1718
    @Test
    public void nested_object_validation() throws Exception
    {
        
        final String locatorTemplate = "//p[@data-error-block-for='%s']";
        
        openLinks("NestedObject Demo (client validation disabled)");

        clickAndWait(SUBMIT);
        
        assertEquals("You must provide a value for Not Null String.", 
                getText(String.format(locatorTemplate, "notNullString")));
        assertEquals("Simple Not Null Property may not be null", 
                getText(String.format(locatorTemplate, "simpleNotNullProperty")));
        assertEquals("Min Value must be greater than or equal to 6", 
                getText(String.format(locatorTemplate, "minValue")));
        
        openLinks("NestedObject Demo (client validation enabled)");
        
        click(SUBMIT);
        assertEquals("You must provide a value for Not Null String.", 
                getText(String.format(locatorTemplate, "notNullString")));
        assertEquals("Simple Not Null Property may not be null", 
                getText(String.format(locatorTemplate, "simpleNotNullProperty")));
        assertEquals("Min Value must be greater than or equal to 6", 
                getText(String.format(locatorTemplate, "minValue")));

    }
    
    //TAP5-1981
    @Test
    public void validate_with_radiogroup() throws Exception
    {
        openLinks("Radio Group with Validation");
        
        click("//div[@class='test1']/form/input[@value='3']");

        clickAndWait("//div[@class='test1']/form/input[@type='submit']");

        assertTextPresent("Radiogroup requires a value no larger than 2.");

        click("//div[@class='test2']/form/input[@value='3']");

        clickAndWait("//div[@class='test2']/form/div/div/input[@type='submit']");

        assertTextPresent("Group2 must be less than or equal to 2");
    }

}