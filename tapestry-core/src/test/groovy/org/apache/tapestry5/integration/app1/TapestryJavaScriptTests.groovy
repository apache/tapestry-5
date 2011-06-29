package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class TapestryJavaScriptTests extends TapestryCoreTestCase {

    @Test
    void basic_javascript_tests() {
        openLinks "JavaScript Unit Tests"

        def caption = getText("//div[@class='js-results']/p[contains(@class,'caption')]")

        def matches = caption =~ /(\d+) failed/

        if (matches[0][1] != "0"){
            fail "Some JavaScript unit tests failed"
        }
    }
}