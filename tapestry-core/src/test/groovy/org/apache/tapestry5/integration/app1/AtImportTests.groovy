package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app1")
class AtImportTests extends GroovyTapestryCoreTestCase
{

    /**
     * Test for https://issues.apache.org/jira/browse/TAP5-2197.
     */
    @Test
    void at_import_without_stack_but_with_stylesheet_gets_core_stack_after_included_stylesheet()
    {
        
        final String locatorTemplate = "//link[contains(@href, 'via-import.css')]/preceding-sibling::link[contains(@href, '%s.css')]"
        
        open "/AtImportWithoutStackButWithStylesheet"
        
        assert isElementPresent(String.format(locatorTemplate, "bootstrap"))
        assert isElementPresent(String.format(locatorTemplate, "tapestry"))
        assert isElementPresent(String.format(locatorTemplate, "exception-frame"))
        assert isElementPresent(String.format(locatorTemplate, "tapestry-console"))
        assert isElementPresent(String.format(locatorTemplate, "tree"))
        
    }

    @Test
    void import_and_asset_injection_in_base_class_finds_resources_there() {
        openLinks "Base class Assets in sub-classes"

        assertText "//h1", "Alpha Library: Logo Demo"
    }

}
