//  Copyright 2023 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package t5.webresources.services.web;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

/**
 * Adapted from WebResourcesSpec.groovy.s
 */
@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class WebResourcesTest extends SeleniumTestCase {

    @Test
    public void test_CoffeeScript_compilation()
    {
        open("/");
        waitForInitializedPage();
        assertEquals(getText("banner"), "Index module loaded, bare!");
    }

    @Test
    public void test_Less_compilation()
    {
        open("/");
        waitForInitializedPage();
        click("css=.navbar .dropdown-toggle");
        click("link=MultiLess");
        waitForInitializedPage();
        waitForCondition("document.getElementById('demo') != null", PAGE_LOAD_TIMEOUT);
        assertEquals(getEval("window.getComputedStyle(document.getElementById('demo'), null).getPropertyValue('background-color')"), "rgb(179, 179, 255)");
    }

    private void waitForInitializedPage() {
        waitForCondition("$('body').attr('data-page-initialized') == 'true' ", PAGE_LOAD_TIMEOUT);
    }

}
