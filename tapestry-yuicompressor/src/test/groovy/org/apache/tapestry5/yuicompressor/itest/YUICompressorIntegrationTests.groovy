// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.yuicompressor.itest

import org.apache.tapestry5.test.SeleniumTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
class YUICompressorIntegrationTests extends SeleniumTestCase
{
    def AVAILABLE_OPTIONS = "css=.t-palette-available select";

    def SELECT_BUTTON = "css=.t-palette [data-action=select]";

    @Test
    void basic_functionality() {

        openBaseURL()

        waitForPageInitialized()

        addSelection AVAILABLE_OPTIONS, "label=Clojure"
        click SELECT_BUTTON

        addSelection AVAILABLE_OPTIONS, "label=Java"
        click SELECT_BUTTON
        
        clickAndWait SUBMIT

        waitForPageInitialized()

        assertText "selected", "CLOJURE, JAVA"
    }

    @Test
    void bad_js_is_reported() {
        openLinks "Bad JavaScript Demo"

        // We still get there, no the exception page.

        assertTitle "Tapestry 5: Bad JavaScript Demo"
    }
}
