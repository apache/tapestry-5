// Copyright 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app5

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

/**
 * Tests for the "skinning support" added in 5.3.
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app5")
class SkinningTests extends TapestryCoreTestCase
{
    @Test
    void template_selection() {
        openLinks "reset session"

        assertTitle "Default Layout"

        clickAndWait "link=Barney Client"

        assertTitle "Barney Layout"

        clickAndWait "link=French"

        assertTitle "Barney Layout (French)"
    }

    @Test
    void application_catalog_overrides() {
        openLinks "reset session"

        assertText "app", "Application catalog message"
        assertText "app-over", "Overridable app catalog message"

        clickAndWait "link=Barney Client"

        assertText "app-over", "Overriden app catalog message (Barney)"
    }

    @Test
    void component_catalog_overrides() {
        openLinks "reset session"

        assertText "page", "Page catalog message"
        assertText "page-over", "Overridable page catalog message"

        clickAndWait "link=Barney Client"

        assertText "page-over", "Overridden page catalog message (Barney)"
    }
    
    @Test
    void injection_of_selector()
    {
        openLinks "reset session", "English"
        
        assertText "selector", "ComponentResourceSelector[en]"
        
        clickAndWait "link=French"
        
        assertText "selector", "ComponentResourceSelector[fr]"
    }
}
