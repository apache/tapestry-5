// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor;
import org.apache.tapestry5.internal.test.InternalBaseTestCase 
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptStack 
import org.apache.tapestry5.services.javascript.JavaScriptStackSource 
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.testng.annotations.Test;

/** 
 * Tests {@link JavaScriptSupport#autofocus(org.apache.tapestry5.FieldFocusPriority, String)}
 * 
 */
class JavaScriptSupportAutofocusTests extends InternalBaseTestCase
{
    private autofocus_template(expectedFieldId, cls) {
        def linker = mockDocumentLinker()
        def stackSource = newMock(JavaScriptStackSource.class)
        def stackPathConstructor = newMock(JavaScriptStackPathConstructor.class)
        def coreStack = newMock(JavaScriptStack.class)
        
        // Adding the autofocus will drag in the core stack
        
        expect(stackSource.getStack("core")).andReturn coreStack
        
        expect(stackPathConstructor.constructPathsForJavaScriptStack("core")).andReturn([])
        
        expect(coreStack.getStacks()).andReturn([])
        expect(coreStack.getStylesheets()).andReturn([])
        expect(coreStack.getInitialization()).andReturn(null)
        
        JSONObject expected = new JSONObject("{\"activate\":[\"$expectedFieldId\"]}")
        
        linker.setInitialization(InitializationPriority.NORMAL, expected)
        
        replay()
        
        def jss = new JavaScriptSupportImpl(linker, stackSource, stackPathConstructor)
        
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
