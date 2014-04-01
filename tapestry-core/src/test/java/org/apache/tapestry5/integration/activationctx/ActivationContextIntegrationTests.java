// Copyright 2014 The Apache Software Foundation
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

package org.apache.tapestry5.integration.activationctx;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

/**
 * To test TAP5-2070
 */
@TapestryTestConfiguration(webAppFolder = "src/test/activationctx")
public class ActivationContextIntegrationTests extends TapestryCoreTestCase
{
    
    private final static String STRING_PARAMETER = null;
    private final static String PAGE_CLASS_PARAMETER = "page class";
    private final static String PAGE_INSTANCE_PARAMETER = "page instance";
    
    @Test
    public void no_context_correct()
    {
        assertLinksAreCorrect("No context correct", "You are able to see me only without activation context");
    }

    @Test
    public void no_context_error()
    {
        openLinks("No context error");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void one_context_without()
    {
        openLinks("One context without");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void one_context_with_one()
    {
        openLinks("One context correct");

        assertTextPresent("You are able to see me only with one parameter in the activation context");
    }

    @Test
    public void one_context_with_two()
    {
        openLinks("One context error");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void two_context_without()
    {
        openLinks("Two context without");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void two_context_with_one()
    {
        openLinks("Two context error");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void two_context_with_two()
    {
        openLinks("Two context correct");

        assertTextPresent("You are able to see me only with two parameters in the activation context");
    }
    
    private void assertLinksAreCorrect(final String label, final String expectedText)
    {
        assertNoContextCorrect(label, STRING_PARAMETER, expectedText);
        assertNoContextCorrect(label, PAGE_CLASS_PARAMETER, expectedText);
        assertNoContextCorrect(label, PAGE_INSTANCE_PARAMETER, expectedText);
    }

    private void assertNoContextCorrect(String label, String suffix, String expectedText)
    {
        String fullLabel = suffix == null ? label : label + ": " + suffix;

        openLinks(fullLabel);

        assertTextPresent(expectedText);
    }

}
