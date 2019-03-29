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

package org.apache.tapestry5.integration.app3;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

/**
 * Additional integration tests that do not fit with the main group due to the need for special
 * configuration.
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app3")
public class AdditionalIntegrationTests extends TapestryCoreTestCase
{
    /**
     * Test to prove that a redirect from the start page works correctly.
     * 
     * @see https://issues.apache.org/jira/browse/TAPESTRY-1627
     */
    @Test
    public void redirect_for_root() throws Exception
    {
        openBaseURL();

        assertText("//h1", "Login Page");
    }

    @Test
    public void bean_block_overrides()
    {
        openLinks("BeanDisplay Override Demo");

        assertText("//dd[@class='no']", "Nay");
        assertText("//dd[@class='yes']", "Yea");
    }

    /**
     * TAPESTRY-2226
     */
    @Test
    public void activation_context_for_root_index_page()
    {
        open(getBaseURL() + "it$0020worked");

        assertText("//h1", "Index");

        assertText("message", "it worked");
    }

    /**
     * TAPESTRY-2217
     */
    @Test
    public void page_document_generator()
    {
        openLinks("PageDocumentGenerator demo");

        // In generated document: not optimized
        assertAttribute("//a[1]/@href", "/login");

        // In normal render: optimized
        // Fuckin Selenium
        // assertAttribute("//a[2]/@href", "login");
    }
    
    // TAP5-1611
    @Test
    public void component_replacer() 
    {
        
        final String[] pageNames = {"ComponentReplacer demo", "ComponentReplacer demo (using @Component to declare component instances)"};
        for (String pageName : pageNames)
        {
            openLinks(pageName);
            
            assertTrue(isElementPresent("overrideMixin"));
            assertFalse(isElementPresent("overridenMixin"));
            assertTrue(isElementPresent("overrideComponent"));
            assertFalse(isElementPresent("overridenComponent"));
        }
        
    }
    
    /** TAP5-1815. In this webapp, HTML5 support is enabled, so we check whether it actually is enabled */
    @Test
    public void html5_support_enabled() throws Exception
    {
        open("/html5support");

        // number translator should cause text fields to have type="number
        assertEquals("number", getAttribute("integer@type"));

        // required attribute for the required validator
        assertEquals("required", getAttribute("required@required"));
        
        // pattern attribute for the regexp validator
        assertEquals("[0-9]{2}", getAttribute("regexp@pattern"));
        
        // type="email" for the email validator
        assertEquals("email", getAttribute("email@type"));

        // type="number" for min validator
        assertEquals("number", getAttribute("minNumber@type"));
        assertEquals("1", getAttribute("minNumber@min"));

        // type="number" for max validator
        assertEquals("number", getAttribute("maxNumber@type"));
        assertEquals("10", getAttribute("maxNumber@max"));

        // type="number" for min and validators togenter
        assertEquals("number", getAttribute("minMaxNumber@type"));
        assertEquals("2", getAttribute("minMaxNumber@min"));
        assertEquals("4", getAttribute("minMaxNumber@max"));

        assertEquals(getAttribute("bool@required"), null);

        assertEquals(getAttribute("mustBeCheckedBoolean@required"), "required");

        assertEquals(getAttribute("requiredBoolean@required"), "required");
    }

}
