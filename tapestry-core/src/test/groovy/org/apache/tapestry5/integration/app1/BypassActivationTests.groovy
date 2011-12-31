package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

/**
 * See https://issues.apache.org/jira/browse/TAP5-1809?jwupdated=41324#linkingmodule
 */
class BypassActivationTests extends TapestryCoreTestCase {

    @Test
    void activation_can_by_bypassed() {

        openLinks "Immediate Response"

        assertText "activated", "false"

        clickAndWait "link=refresh"

        assertText "activated", "true"
    }
}
