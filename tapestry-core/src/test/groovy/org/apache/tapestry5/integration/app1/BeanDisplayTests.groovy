package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app1")
class BeanDisplayTests extends GroovyTapestryCoreTestCase
{

    /** TAP5-2270 */
    @Test
    void beandisplay_inside_form()
    {
        open "/BeanDisplayInsideForm"
        click "submit"
        waitForPageToLoad()
        assert isElementPresent("css=div.panel-danger") == false
    }

}
