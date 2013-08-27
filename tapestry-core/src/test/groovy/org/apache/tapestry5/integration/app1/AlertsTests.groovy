// Copyright 2011-2013 The Apache Software Foundation
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


import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

/**
 * Tests for the client-side Alerts.
 *
 * @since 5.3
 */
class AlertsTests extends TapestryCoreTestCase {

    def CONTAINER = "[data-container-type=alerts]"

    @Test
    void traditional_update_and_remove() {
        openLinks "Alerts Demo", "Reset Alerts Storage"

        select "id=severity", "Warn"
        select "id=duration", "Until Dismissed"
        type "id=message", "trad warn until"

        clickAndWait "//input[@value='Traditional Update']"

        waitForCSSSelectedElementToAppear "$CONTAINER .alert"

        assertTextPresent "trad warn until"

        clickAndWait BACK_TO_INDEX

        waitForCSSSelectedElementToAppear "$CONTAINER .alert"

        assertTextPresent "trad warn until"

        // Click the first and only dismiss icon

        click "css=$CONTAINER button.close"

        sleep AJAX_WAIT_TIME

        // Check that the alert container is now empty

        assertText "css=$CONTAINER", ""
    }

    @Test
    void remove_alert_container_with_last_transient_alert() {
        openLinks "Alerts Demo", "Reset Alerts Storage"

        select "id=severity", "Warn"
        select "id=duration", "Transient"
        type "id=message", "trad warn transient"

        clickAndWait "//input[@value='Traditional Update']"

        waitForCSSSelectedElementToAppear "$CONTAINER .alert"

        assertTextPresent "trad warn transient"

        // dismiss the first alert that indicates the submission type

        click "css=$CONTAINER :contains('Traditional form submission') button.close"

        // wait for the transient alert to be automatically removed
        sleep 5000

        // Check that the alert container is now empty

        assertText "css=$CONTAINER", ""
    }

    @Test
    void ajax_update_and_remove() {
        openLinks "Alerts Demo", "Reset Alerts Storage"

        select "css=#ajax select[name=\"severity\"]", "Error"
        select "css=#ajax select[name=\"duration\"]", "Until Dismissed"
        type "css=#ajax input[name=\"message\"]", "ajax error until"

        click "//input[@value='Ajax Update']"

        waitForCSSSelectedElementToAppear "$CONTAINER .alert"

        assertTextPresent "ajax error until"

        click "css=$CONTAINER [data-action='dismiss-all']"

        // Check that the alert container is now empty

        assertText "css=$CONTAINER", ""
    }

    /** Disabled by HLS 7-oct-2011; there's a timing issue that makes it very fragile.   */
    @Test(enabled = false)
    void ajax_update_with_redirect() {

        openLinks "Alerts Demo", "Reset Alerts Storage"

        select "css=#ajax select[name=\"severity\"]", "Error"
        select "css=#ajax select[name=\"duration\"]", "Single"
        type "css=#ajax input[name=\"message\"]", "ajax error single"
        check "css=#ajax input[type='checkbox']"

        click "//input[@value='Ajax Update']"

        sleep AJAX_WAIT_TIME

        waitForCSSSelectedElementToAppear "div.t-error"

        assertText "css=div.t-error div.t-message-container", "ajax error single"

        click "link=Dismiss all"
    }

    @Test
    void check_informal_parameters() {
        openLinks "Alerts Demo"

        assertTrue isElementPresent("css=${CONTAINER}.alert-class")
    }

}
