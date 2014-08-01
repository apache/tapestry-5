package org.apache.tapestry5.integration.app5

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test


@TapestryTestConfiguration(webAppFolder = "src/test/app5")
class ProductionModeTests extends TapestryCoreTestCase {

    @Test
    void invalid_component_id_is_404()
    {
        openBaseURL()

        assertTitle "Default Layout"

        def invalid = new URL("${baseURL}index.missing")

       assertEquals 404, invalid.openConnection().responseCode
    }

}
