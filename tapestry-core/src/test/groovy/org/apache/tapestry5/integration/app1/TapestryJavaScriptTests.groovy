package org.apache.tapestry5.integrati.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class TapestryJavaScriptTests extends TapestryCoreTestCase {

    @Test
    void basic_javascript_tests() {
        openLinks "JavaScript Unit Tests"

        def resultClass = getAttribute("//table[@class='js-results']/caption/@class")
        
        if (resultClass == 'failures') {
            fail "Some JavaScript unit tests failed"
        }
    }
}