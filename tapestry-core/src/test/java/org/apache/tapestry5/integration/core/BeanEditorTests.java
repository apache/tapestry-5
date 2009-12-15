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

import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for the {@link BeanEditor} component, often embedded inside
 * a {@link BeanEditForm} component.
 */
public class BeanEditorTests extends TapestryCoreTestCase
{
    /**
     * Tests the bean editor. Along the way, tests a bunch about validation,
     * loops, blocks, and application state
     * objects.
     */
    @Test
    public void bean_editor()
    {
        clickThru("BeanEditor Demo", "Clear Data");

        clickAndWait(SUBMIT);

        // Part of the override for the firstName property

        assertAttribute("//input[@id='firstName']/@size", "40");

        // Check that the @Width annotation works

        assertAttribute("//input[@id='birthYear']/@size", "4");

        // Check override of the submit label

        assertAttribute("//input[@type='submit']/@value", "Register");

        type("firstName", "a");
        type("lastName", "b");
        type("birthYear", "");
        select("sex", "label=Martian");
        click("citizen");
        type("password", "abracadabra");
        type("notes", "line1\nline2\nline3");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide at least 3 characters for First Name.",
                "You must provide at least 5 characters for Last Name.",
                "You must provide a value for Year of Birth.");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1966");
        type("password", "supersecret");

        clickAndWait(SUBMIT);

        // The XPath support is too weak for
        // //div[@class='t-beandisplay-value'][%d], so we
        // just look for the text itself.

        assertTextPresent("Howard", "Lewis Ship", "1966", "Martian", "U.S. Citizen", "***********",
                "line1", "line2", "line3");
    }

    @Test
    public void bean_editor_property_reorder_remove()
    {
        clickThru("BeanEdit Remove/Reorder", "Clear Data");

        // Looks like a bug in Selenium; we can see //label[1] but not
        // //label[2].
        // assertTextSeries("//label[%d]", 1, "Last Name", "First Name", "Sex",
        // "U.S. Citizen");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("password", "supersecret");
        check("citizen");

        clickAndWait("//input[@type=\'submit\']");

        assertTextPresent("Howard", "Lewis Ship", "0", "100% He-Man", "U.S. Citizen");
    }
}
