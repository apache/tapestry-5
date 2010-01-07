// Copyright 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

@Test(sequential = true, groups = "integration")
public class TapestryBeanValidationIntegrationTests extends AbstractIntegrationTestSuite
{
    public TapestryBeanValidationIntegrationTests()
    {
        super("src/test/webapp");
    }

    public void form_validation() throws Exception
    {
    	start("Form Validation Demo");
    	
    	//Test JSR-303 validator
    	
    	clickAndWait(SUBMIT);

        assertTextPresent("User Name may not be null");
        
        //Test Tapestry validator
        
        type("userName", "igor");
    	
    	clickAndWait(SUBMIT);
    	
    	assertTextPresent("You must provide at least 5 characters for User Name.");
        
        type("userName", "igor123");
    	
    	clickAndWait(SUBMIT);
    }

    public void beaneditform_validation() throws Exception
    {
    	start("BeanEditForm Validation Demo");
    	
    	clickAndWait(SUBMIT);
    	
        assertTextPresent("User Name may not be null", "Password may not be null");
        
        type("userName", "igor");
    	
    	clickAndWait(SUBMIT);
    	
        assertTextPresent("User Name size must be between 7 and 10", "User Name must match \"[0-9]+\"");
    }
    
    public void inject_validator() throws Exception
    {
    	start("Inject Validator Demo");
    	
    	clickAndWait(SUBMIT);

    	assertTextPresent("User Name may not be null");
    }
    
    public void client_validaton() throws Exception
    {
    	start("Client Validation Demo");
    	
    	click(SUBMIT);

    	assertBubbleMessage("userName", "may not be null");
    	
    	type("userName", "igor");
    	
    	click(SUBMIT);

    	assertBubbleMessage("password", "may not be null");
    }
    
    protected final void assertBubbleMessage(String fieldId, String expected)
    {
        String popupId = fieldId + ":errorpopup";

        waitForElementToAppear(popupId);

        assertText(String.format("//div[@id='%s']/span", popupId), expected);
    }
    
    protected final void waitForElementToAppear(String elementId)
    {
        String condition = String.format("window.$(\"%s\")", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }
}