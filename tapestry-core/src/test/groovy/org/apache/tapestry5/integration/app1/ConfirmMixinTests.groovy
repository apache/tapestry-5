package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test


class ConfirmMixinTests extends App1TestCase {

    @Test
    void confirm_mixin() {
        openLinks "Confirm Mixin Demo"

        click "link=Click This"

        waitForVisible "css=.modal-dialog"

        clickAndWait "css=.modal-dialog .btn-warning"

        assertFirstAlert("Action was confirmed.")
    }

}
