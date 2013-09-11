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

import org.testng.annotations.Test;

public class LoopTests extends App1TestCase
{
    /**
     * TAP5-205
     */
    @Test
    public void handling_of_empty_loop()
    {
        openLinks("Empty Loop Demo");

        assertText("first", "");
        assertText("second", "Source is null.");
        assertText("third", "Source is the empty list.");
    }

    @Test
    public void encoded_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List");
    }

    @Test
    public void volatile_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List (Volatile)");
    }

    @Test
    public void generic_loop()
    {
        openLinks("Generic Loop Demo");
        String[] strings = {"1", "3", "5", "7", "11"};
        for ( int i = 0; i< strings.length; ++i)
        {
            assertText("int_" + i, strings[i]);
        }

        strings = new String[] {"John Doe", "Jane Dover", "James Jackson"};
        for ( int i = 0; i< strings.length; ++i)
        {
            assertText("person_" + i, strings[i]);
        }

        strings = new String[] {"DOG:6:Dakota", "CAT:3:Jill", "CAT:3:Jack"};
        for (int i = 0; i< strings.length; ++i)
        {
            assertText("inherited_" + i, strings[i]);
        }
    }

    private void test_loop_inside_form(String linkLabel)
    {
        openLinks(linkLabel);

        clickAndWait("link=reset the database");

        assertFieldValue("title", "End World Hunger");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel");
        assertFieldValue("title_1", "Cure Common Cold");

        type("title", "End World Hunger - today");
        type("title_0", "Develop Faster-Than-Light Travel - immediately");
        type("title_1", "Cure Common Cold - post haste");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");

        clickAndWait("//input[@value='Add new ToDo']");

        type("title_2", "Conquer World");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");
        assertFieldValue("title_2", "Conquer World");
    }

    @Test
    public void after_render_does_not_shortcut_other_after_render_phase_methods() throws Exception{
        openLinks("Loop With Mixin Demo");

        assertTextPresent("BEGIN-TRACER-MIXIN");
        assertTextPresent("123456");
        assertTextPresent("123457");
        assertTextPresent("AFTER-TRACER-MIXIN");
    }
}
