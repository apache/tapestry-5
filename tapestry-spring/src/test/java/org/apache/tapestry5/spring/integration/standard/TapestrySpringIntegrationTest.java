// Copyright 2007, 2008, 2009, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.spring.integration.standard;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class TapestrySpringIntegrationTest extends SeleniumTestCase
{


    @Test
    public void integration_test() throws Exception
    {
        openBaseURL();

        type("input", "paris in the springtime");
        clickAndWait("//input[@value='Convert']");

        assertFieldValue("input", "PARIS IN THE SPRINGTIME");
    }

    @Test
    public void access_to_spring_context() throws Exception
    {
        openBaseURL();

        String text = getText("beans");
        assertTrue(text.contains("upcase"));
    }

    @Test
    public void customize_pipeline_is_invoked() throws Exception
    {
        openBaseURL();

        assertText("message", "SPRING VERSION 3.2.9.RELEASE: PIPELINE WAS INVOKED");
    }

    @Test
    public void too_many_spring_beans_are_assignable()
    {
        open(getBaseURL() + "bedrock");

        assertTextPresent(
                "Spring context contains 2 beans assignable to type org.example.testapp.services.Flintstone: barney, fred.");
    }

    @Test
    public void factory_provided_beans_accessible() throws Exception
    {
        openBaseURL();

        assertEquals(getText("viaFactory"), "Instantiated via a factory bean.");
    }

    @Test
    public void symbol_bean_factory_post_processor() throws Exception
    {
        openBaseURL();

        assertEquals(getText("symbolValueHolder"), "Start page is 'start', Value from Spring is 'Hello'");
    }

    @Test
    public void no_conflict_on_injected_locale() throws Exception
    {
        openBaseURL();

        assertEquals(getText("locale"), "en");
        assertEquals(getText("selector"), "en");
    }
}
