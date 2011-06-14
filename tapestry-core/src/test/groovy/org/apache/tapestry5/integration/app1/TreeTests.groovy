// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.test.SeleniumTestCase
import org.testng.annotations.Test

class TreeTests extends SeleniumTestCase
{
    /**
     * Haven't figured out how to get Selenium to click the actual elements, never mine
     * coordinate the wait for the Ajax. So far its just been manual testing.
     */
    @Test
    void basics() {

        openBaseURL()

        clickAndWait "link=Tree Component Demo"

        clickAndWait "link=clear expansions"

        if (false) {
            click "//span[@class='t-tree-icon'][2]"

            sleep 100

            click "//span[@class='t-tree-icon'][3]"

            sleep 100

            assertTextPresent "Agricola"

            clickAndWait "link=Redraw"

            assertTextPresent "Agricola"
        }
    }
}
