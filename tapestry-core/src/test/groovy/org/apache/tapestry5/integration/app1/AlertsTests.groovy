// Copyright  2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;


import org.apache.tapestry5.test.SeleniumTestCase
import org.testng.annotations.Test

/**
 * Tests for the client-side Alerts.
 *
 * @since 5.3
 */
class AlertsTests extends SeleniumTestCase
{
    @Test
    void traditional_update_and_remove()
    {
        openLinks "Alerts Demo", "reset"

        select "id=severity", "Warn"
        select "id=duration", "Until Dismissed"
        type "id=message", "trad warn until"

        clickAndWait "//input[@value='Traditional Update']"

        assertTextPresent "trad warn until"

        clickAndWait "link=Back to index"

        assertTextPresent "trad warn until"
    }
}