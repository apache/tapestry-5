// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.integration.app1.data.RegistrationData;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

/**
 * Tests for the {@link BeanEditor} component, often embedded inside
 * a {@link BeanEditForm} component.
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app1")
@Test
public class BeanEditorTests extends App1TestCase
{
    /**
     * Tests the bean editor. Along the way, tests a bunch about validation,
     * loops, blocks, and application state
     * objects.
     */
    @Test
    public void bean_editor()
    {
        openLinks("BeanEditor Demo", "Clear Data");

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
        openLinks("BeanEdit Remove/Reorder", "Clear Data");

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

    @Test
    public void multiple_beaneditor_components()
    {
        openLinks("MultiBeanEdit Demo", "Clear Data");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("path", "/var/www");
        clickAndWait("//input[@value='Set Access']");

        assertTextSeries("//ul[@id='results']//li[%d]", 1, "First Name: [Howard]", "Last Name: [Lewis Ship]",
                "Path: [/var/www]", "Role: [GRANT]");
    }

    /**
     * This also checks that the date type is displayed correctly by BeanDisplay
     * and Grid.
     */
    @Test
    public void date_field_inside_bean_editor()
    {
        openLinks("BeanEditor / Date Demo", "clear");

        type("name", "Howard Lewis Ship");
        type("date", "12/24/1966");

        clickAndWait(SUBMIT);

        // Notice the date output format; that is controlled by the date Block
        // on the
        // PropertyDisplayBlocks page.

        assertTextPresent("Howard Lewis Ship", "Dec 24, 1966");
    }

    /**
     * TAPESTRY-2013
     */
    @Test
    public void bean_editor_overrides()
    {
        openLinks("BeanEditor Override", "Clear Data");

        assertTextPresent("[FirstName Property Editor Override]");
    }

    /**
     * TAPESTRY-1869
     */
    @Test
    public void null_fields_and_bean_editor()
    {
        openLinks("Number BeanEditor Demo");

        clickAndWait(SUBMIT);

        // Hard to check for anything here.

        clickAndWait("link=Back to form");

        type("value", "237");

        clickAndWait(SUBMIT);

        assertText("//dd[@class='value']", "237");
    }

    // TAPESTRY-2460

    @Test
    public void nested_bean_editor_and_bean_display()
    {
        openLinks("Nested BeanEditor");

        type("name", "Parent");
        type("age", "60");

        type("name_0", "Child");
        type("age_0", "40");

        clickAndWait(SUBMIT);


        assertTextPresent("Nested BeanDisplay");

        // As usual, Selenium is fighting me in terms of extracting data, so the
        // above check just ensures
        // we made it past the form submit without error.
    }

    /**
     * TAPESTRY-2592
     */
    @Test
    public void bean_editor_pushes_bean_edit_context()
    {
        openLinks("BeanEditor BeanEditContext");

        assertTextPresent("Bean class from context is: " + RegistrationData.class.getName());
    }
    
    @Test
    public void object_editor_test() {
        openLinks("Object Editor Demo");
        assertTextPresent("Street");
    }
    
    /** TAP5-991 */
    public void bean_display_enum_value_from_messages()
    {
        openLinks("BeanDisplay Enum Demo");
        
        assertText("//dd[2]", "Ultra Important");
    }

    /** TAP5-1527 */
    public void bean_editor_prepare_bubbling()
    {
        openLinks("BeanEditor Prepare Bubbling Demo");

        type("name", "abcdef");
        type("age", "10");

        clickAndWait(SUBMIT);

        assertTextPresent("Name: abcdef", "Age: 10");
    }
    
    /** TAP5-2182 */
    public void bean_editor_default_markup() {

        final String formGroupLocator = "//form[@id='form']/div[@class='form-group'][2]";
        final String wrapperCssLocator = formGroupLocator + "/@class";
        final String labelCssLocator = formGroupLocator + "/label/@class";
        final String inputCssLocator = formGroupLocator + "/input/@class";
        
        openLinks("BeanEditor Demo");
        
        assertEquals("form-group", getAttribute(wrapperCssLocator));
        assertEquals("control-label", getAttribute(labelCssLocator));
        assertEquals("form-control", getAttribute(inputCssLocator));
        
    }
    
    /** TAP5-2662 */
    public void bean_editor_accessibility() 
    {

        openLinks("BeanEditor Demo");
        
        assertEquals("lastName-label", getAttribute("//input[@id='lastName']/@aria-labelledby"));
        assertEquals("lastName", getAttribute("//label[@id='lastName-label']/@for"));
        
    }
    
}
