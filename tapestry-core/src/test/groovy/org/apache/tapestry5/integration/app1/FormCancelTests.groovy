package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test


class FormCancelTests extends App1TestCase {

    @Test
    void cancel_events_are_triggered_when_form_submitted() {

        openLinks "Form Cancel Action Demo", "Reset Page State"

        clickAndWait SUBMIT

        assertText "css=#messages > li:first", "action trigger"
        assertText "css=#messages > li:nth(1)", "cancel event"
    }

}
