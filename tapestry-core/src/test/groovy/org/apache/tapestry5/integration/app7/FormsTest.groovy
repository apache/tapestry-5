package org.apache.tapestry5.integration.app7

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app7")
class FormsTest extends GroovyTapestryCoreTestCase
{
	
	/** TAP5-2715 */
	@Test
	void client_side_generated_validation_error_message()
	{
		open "/Forms"
		assert !isElementPresent('xpath=//*[@id="foo-help-block"]')
		click "submit"
		waitForPageToLoad()
		def actual = getAttribute('xpath=//*[@id="foo-help-block"]/@class')
		assert actual.contains("form-text")
		assert actual.contains("text-danger")
	}

}
