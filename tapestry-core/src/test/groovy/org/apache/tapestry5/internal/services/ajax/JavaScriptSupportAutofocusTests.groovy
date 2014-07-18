// Copyright 2010-2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.ajax

import org.apache.tapestry5.FieldFocusPriority
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.services.javascript.InitializationPriority
import org.apache.tapestry5.services.javascript.JavaScriptStackSource
import org.apache.tapestry5.services.javascript.JavaScriptSupport
import org.testng.annotations.Test

/**
 * Tests {@link JavaScriptSupport#autofocus(org.apache.tapestry5.FieldFocusPriority, String)}
 *
 */
class JavaScriptSupportAutofocusTests extends InternalBaseTestCase {

    private autofocus_template(expectedFieldId, cls) {
        def linker = mockDocumentLinker()
        def stackSource = newMock(JavaScriptStackSource.class)
        def stackPathConstructor = newMock(JavaScriptStackPathConstructor.class)

        expect(stackSource.stackNames).andReturn([])

        linker.addInitialization(InitializationPriority.NORMAL, "t5/core/pageinit", "focus",
            JSONArray.from([expectedFieldId]))

        replay()

        // Test in partial mode, to bypass the logic about importing the "core' stack.
        def jss = new JavaScriptSupportImpl(linker, stackSource, stackPathConstructor, null, true, null)

        cls jss

        jss.commit()

        verify()
    }

    @Test
    void simple_autofocus() {

        autofocus_template "fred", {
            it.autofocus FieldFocusPriority.OPTIONAL, "fred"
        }
    }

    @Test
    void first_focus_field_at_priority_wins() {
        autofocus_template "fred", {
            it.autofocus FieldFocusPriority.OPTIONAL, "fred"
            it.autofocus FieldFocusPriority.OPTIONAL, "barney"
        }
    }

    @Test
    void higher_priority_wins_focus() {
        autofocus_template "barney", {
            it.autofocus FieldFocusPriority.OPTIONAL, "fred"
            it.autofocus FieldFocusPriority.REQUIRED, "barney"
        }
    }
}
