package org.apache.tapestry5.integration.app7

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app7")
class PopoverTest extends GroovyTapestryCoreTestCase
{
	
	/** TAP5-2686 */
	@Test
	void popover_js_accessible()
	{
		open "/modules.gz/bootstrap/popover.js"
		waitForPageToLoad()
		assertTextPresent("Bootstrap popover.js v4.3.1 (https://getbootstrap.com/)")
	}

}
