package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

class SubmitUnconditionalTests extends App1TestCase {

    @Test
    void submit_with_unconditional_mode() {
        openLinks "Cancel Demo"

        clickAndWait "//input[@value='Abort']"

        assertText "message", "onSelectedFromAbort() invoked."
    }

    @Test
    void LinkSubmit_with_unconditional_mode() {

        openLinks "Cancel Demo"

        clickAndWait "link=Abort"

        assertText "message", "onSelectedFromAbortLink() invoked."
    }
}
