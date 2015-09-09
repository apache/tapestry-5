package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app1")
class BeanEditorWithFormFragmentTests extends GroovyTapestryCoreTestCase
{

    /** TAP5-2308 */
    @Test
    void beaneditor_with_formfragment_and_triggerfragment_mixin()
    {
        openLinks("Bean Editor With Form Fragment Demo", "Reset Page State")
        assert isVisible("css=#address")
        click "css=#canBeDoneRemotely"
        assert !isVisible("css=#address")
    }
    
    /** TAP5-2308 */
    @Test
    void beaneditor_with_formfragment_and_triggerfragment_mixin_within_loop()
    {
        openLinks("Bean Editor With Form Fragment Demo", "Reset Page State")
        assert isVisible("css=#address")
        assert isVisible("css=#address_0")
        click "css=#canBeDoneRemotely"
        assert !isVisible("css=#address")
        assert isVisible("css=#address_0")
        click "css=#canBeDoneRemotely_0"
        assert !isVisible("css=#address")
        assert !isVisible("css=#address_0")
    }

}
