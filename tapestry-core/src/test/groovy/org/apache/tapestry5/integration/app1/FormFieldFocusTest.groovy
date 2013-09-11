// Copyright 2011-2013 The Apache Software Foundation
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

import org.testng.annotations.Test

/**
 * Integration test to veryfiy {@link com.gargoylesoftware.htmlunit.javascript.host.FormField}
 * and {@link org.apache.tapestry5.corelib.mixins.OverrideFieldfocus} mixin.
 */
class FormFieldFocusTest extends App1TestCase
{
    @Test
    void form_field_focus_mixin()
    {
        openLinks "FormFieldFocus (DEPRECATED) Demo"

        sleep 250

        assertEquals getEval("window.document.activeElement.value"), "But I got the focus!"

    }

    @Test
    void override_field_focus_mixin()
    {
        openLinks "OverrideFieldFocus Demo"

        sleep 250

        assertEquals getEval("window.document.activeElement.value"), "But I got the focus!"

    }
}
