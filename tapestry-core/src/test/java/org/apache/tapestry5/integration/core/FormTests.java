// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.core;

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Form} component as well as many form control components.
 */
public class FormTests extends TapestryCoreTestCase
{

    @Test
    public void form_encoding_type()
    {
        clickThru("Form Encoding Type");

        assertAttribute("//form/@enctype", "x-override");
    }

    @Test
    public void page_context_in_form()
    {
        clickThru("Page Context in Form");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces",
                "context/with/slashes");
        assertFieldValue("t:ac",
                "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");

        clickAndWait(SUBMIT);

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces",
                "context/with/slashes");
        assertFieldValue("t:ac",
                "betty/wilma/context$0020with$0020spaces/context$002fwith$002fslashes");
    }
}
