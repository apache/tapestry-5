// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.GroovyTapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

/**
 * Tests for https://issues.apache.org/jira/browse/TAP5-2197.
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app1")
class StylesheetPlacementTests extends GroovyTapestryCoreTestCase
{

    final public String stylesheetTemplate = "//link[contains(@href, 'via-import.css')]/preceding-sibling::link[contains(@href, '%s.css')]"
    final public String styleTemplate = "//style/preceding-sibling::link[contains(@href, '%s.css')]"
    
    @Test
    void at_import_without_stack_but_with_stylesheet_gets_core_stack_after_included_stylesheet()
    {
        open("/AtImportWithoutStackButWithStylesheet")
        testStylesheetsOrdering(stylesheetTemplate)
    }

    @Test
    void stylesheet_included_in_head_without_stack_but_with_stylesheet_gets_core_stack_after_included_stylesheet()
    {
        open("/StylesheetIncludedInHead")
        testStylesheetsOrdering(stylesheetTemplate)
    }
    
    @Test
    void style_element_should_be_placed_before_core_stack_stylesheets()
    {
        open("/StyleElementAndCoreStack")
        testStylesheetsOrdering(styleTemplate)
    }

    @Test
    void link_then_style_element_should_be_placed_before_core_stack_stylesheets()
    {
        open("/LinkThenStyleElementAndCoreStack")
        testStylesheetsOrdering(styleTemplate)
        testStylesheetsOrdering(stylesheetTemplate)
    }

    @Test
    void style_then_link_element_should_be_placed_before_core_stack_stylesheets()
    {
        open("/StyleThenLinkElementAndCoreStack")
        testStylesheetsOrdering(styleTemplate)
        testStylesheetsOrdering(stylesheetTemplate)
    }

    private testStylesheetsOrdering(String locatorTemplate) {
        assert isElementPresent(String.format(locatorTemplate, "bootstrap"))
        assert isElementPresent(String.format(locatorTemplate, "tapestry"))
        assert isElementPresent(String.format(locatorTemplate, "exception-frame"))
        assert isElementPresent(String.format(locatorTemplate, "tapestry-console"))
        assert isElementPresent(String.format(locatorTemplate, "tree"))
    }

}
