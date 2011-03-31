// Copyright 2007, 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app3;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Additional integration tests that do not fit with the main group due to the need for special
 * configuration.
 */
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

    public void ajax_server_side_exception()
    {
        openLinks("Console demo");

        assertTextPresent("Demonstrates Tapestry console");

        click("link=Failure on the server side");

        // Wait for the console to appear

        waitForCSSSelectedElementToAppear(".t-console div.t-err");

        assertTextPresent("Communication with the server failed: Server-side exception.");
    }
}
