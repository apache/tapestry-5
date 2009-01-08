// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

public class TapestrySpringIntegrationTest extends AbstractIntegrationTestSuite
{
    public TapestrySpringIntegrationTest()
    {
        super("src/test/webapp");
    }

    @Test
    public void integration_test() throws Exception
    {
        open(BASE_URL);

        type("input", "paris in the springtime");
        clickAndWait("//input[@value='Convert']");

        assertFieldValue("input", "PARIS IN THE SPRINGTIME");
    }

    @Test
    public void access_to_spring_context() throws Exception
    {
        open(BASE_URL);

        String text = getText("beans");
        assertTrue(text.contains("upcase"));
    }

    @Test
    public void customize_pipeline_is_invoked() throws Exception
    {
        open(BASE_URL);

        assertText("message", "SPRING VERSION 2.5.6: PIPELINE WAS INVOKED");
    }

    @Test
    public void too_many_spring_beans_are_assignable()
    {
        open(BASE_URL + "bedrock");

        assertTextPresent(
                "Spring context contains 2 beans assignable to type org.example.testapp.services.Flintstone: barney, fred.");
    }

    @Test
    public void factory_provided_beans_accessible() throws Exception
    {
        open(BASE_URL);

        assertEquals(getText("viaFactory"), "Instantiated via a factory bean.");
    }
}
