// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.corelib.components.Grid;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Grid} component.
 */
public class GridTests extends App1TestCase
{

    private static final String RESET = "Reset the Grid";

    /**
     * Basic Grid rendering, with a column render override. Also tests sorting.
     */
    @Test
    public void basic_grid()
    {
        openLinks("Grid Demo", RESET);

        // "Sort Rating" via the header cell override (TAPESTRY-2081)

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count", "Sort Rating");

        // Strange: I thought tr[1] was the header row ???

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip", "Electronica", "4", "-");

        // Here were checking that the page splits are correct

        clickAndWait("link=3");

        // Last on page 3:
        assertText("//tr[25]/td[1]", "Blood Red River");

        clickAndWait("link=4");
        assertText("//tr[1]/td[1]", "Devil Song");

        clickAndWait("link=7");
        clickAndWait("link=10");

        // Here's one with a customized rating cell

        assertTextSeries("//tr[25]/td[%d]", 1, "Smoked", "London (Original Motion Picture Soundtrack)",
                "The Crystal Method", "Soundtrack", "30", "****");

        clickAndWait("link=69");

        assertText("//tr[22]/td[1]", "radioioAmbient");

        // Sort ascending (and we're on the last page, with the highest
        // ratings).

        clickAndWait("link=Sort Rating");

        assertTextSeries("//tr[22]/td[%d]", 1, "Mona Lisa Overdrive", "Labyrinth", "Juno Reactor", "Dance", "31",
                "*****");

        // Toggle to sort descending

        clickAndWait("link=Sort Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Hey Blondie", "Out from Out Where");

        clickAndWait("link=Title");

        assertAttribute("//th/@data-grid-column-sort", "ascending");

        clickAndWait("link=1");

        assertText("//tr[1]/td[1]", "(untitled hidden track)");

        clickAndWait("link=Title");

        assertAttribute("//th/@data-grid-column-sort", "descending");

        clickAndWait("link=" + RESET);

        // Back to where we started.

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip", "Electronica", "4", "-");
    }

    @Test
    public void grid_remove_reorder()
    {
        openLinks("Grid Remove/Reorder Demo");

        assertTextSeries("//th[%d]", 1, "Rating", "Title", "Album", "Artist", "Genre");
    }

    @Test
    public void grid_set() throws Exception
    {
        openLinks("Grid Set Demo");

        assertFalse(isTextPresent("Exception"));

        // Also check for TAPESTRY-2228

        assertAttribute("//table/@informal", "supported");
    }

    @Test
    public void grid_from_explicit_interface_model()
    {
        openLinks("SimpleTrack Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "-");
    }

    @Test
    public void grid_enum_display()
    {
        openLinks("Grid Enum Demo", "reset");

        assertTextSeries("//tr[1]/td[%d]", 1, "End World Hunger", "Medium");
        assertTextSeries("//tr[2]/td[%d]", 1, "Develop Faster-Than-Light Travel", "Ultra Important");
        assertTextSeries("//tr[3]/td[%d]", 1, "Cure Common Cold", "Low");
    }

    @Test
    public void null_grid() throws Exception
    {
        openLinks("Null Grid");

        assertTextPresent("There is no data to display.");
    }
    

    // TAP5-244
    @Test
    public void empty_grid_with_columns() throws Exception
    {
        openLinks("Empty Grid Demo");
        
        assertText("//th[1]", "Random");

        assertText("//table/tbody/tr/td[@colspan='1']", "There is no data to display.");
    }
    
    // TAP5-2515
    @Test
    public void empty_grid_with_columns_and_empty_parameter() throws Exception
    {
        openLinks("Empty Grid Demo");
        
        assertText("//table[2]/thead/tr/th[1]", "Random");

        assertText("//table[2]/tbody/tr/td[@colspan='1']", "No data, dude!");
    }
    
    // TAP5-244
    @Test
    public void empty_grid_with_columns_requires_model_parameter() throws Exception
    {
        openLinks("Empty Grid Demo", "Remove the Grid's model");
        
        assertTextPresent("You should bind the model parameter explicitly.");
    }
    
    @Test
    public void grid_inside_form()
    {
        openLinks("Grid Form Demo", "reset", "2");

        // The first input field is the form's hidden field.

        assertFieldValue("title", "ToDo # 6");
        assertFieldValueSeries("title_%d", 0, "ToDo # 7", "ToDo # 8", "ToDo # 9", "ToDo # 10");

        type("title_0", "Cure Cancer");
        select("urgency_0", "Top Priority");

        type("title_1", "Pay Phone Bill");
        select("urgency_1", "Low");

        clickAndWait(SUBMIT);

        assertFieldValueSeries("title_%d", 0, "Cure Cancer", "Pay Phone Bill");
        assertFieldValueSeries("urgency_%d", 0, "HIGH", "LOW");
    }

    @Test
    public void grid_inside_form_with_encoder()
    {
        openLinks("Grid Form Encoder Demo", "reset", "2");

        // The first input field is the form's hidden field.

        // Note the difference: same data sorted differently (there's a default
        // sort).

        assertFieldValue("title", "ToDo # 14");
        assertFieldValueSeries("title_%d", 0, "ToDo # 15", "ToDo # 16", "ToDo # 17", "ToDo # 18");

        type("title_0", "Cure Cancer");
        select("urgency_0", "Top Priority");

        type("title_1", "Pay Phone Bill");
        select("urgency_1", "Low");

        clickAndWait(SUBMIT);

        // Because of the sort, the updated items shift to page #1

        clickAndWait("link=1");

        assertFieldValue("title", "Cure Cancer");
        assertFieldValue("title_0", "Pay Phone Bill");

        assertFieldValue("urgency", "HIGH");
        assertFieldValue("urgency_0", "LOW");
    }

    /**
     * TAP5-2470
     */
    @Test
    public void grid_inside_form_with_initial_sort_mixin()
    {
        openLinks("Grid Form With Initial Sort Mixin Demo", "reset", "2");

        // The first input field is the form's hidden field.

        // Note the difference: same data sorted differently (there's a default
        // sort).

        assertFieldValue("title", "ToDo # 14");
        assertFieldValueSeries("title_%d", 0, "ToDo # 15", "ToDo # 16", "ToDo # 17", "ToDo # 18");

        type("title_0", "Cure Cancer");
        select("urgency_0", "Top Priority");

        type("title_1", "Pay Phone Bill");
        select("urgency_1", "Low");

        clickAndWait(SUBMIT);

        // Because of the sort, the updated items shift to page #1

        clickAndWait("link=1");

        assertFieldValue("title", "Cure Cancer");
        assertFieldValue("title_0", "Pay Phone Bill");

        assertFieldValue("urgency", "HIGH");
        assertFieldValue("urgency_0", "LOW");
    }

    /**
     * TAP5-2470
     */
    @Test
    public void change_model_of_grid_in_a_loop()
    {
        openLinks("Grid In Loop Demo", "reset the Grids");

        for (int i = 0; i < 5; i++)
        {
            String locator = String.format("grid%d", i + 1);
            //Starting with 6 columns every iteration should result to one less column
            int expected = 6 - i;
            String count = getEval("window.document.getElementById('" + locator + "').rows[0].cells.length");
            assertEquals(count, Integer.toString(expected), String.format("Expected %d columns.",expected));
        }
    }

    /**
     * TAPESTRY-2021
     */
    @Test
    public void lean_grid()
    {
        openLinks("Lean Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count", "Rating");

        // Selenium makes it pretty hard to check for a missing class.

        // assertText("//th[1]/@class", "");
    }

    /**
     * TAPESTRY-1310
     */
    @Test
    public void grid_row_and_column_indexes()
    {
        openLinks("Lean Grid Demo", "2");

        // Use page 2 to ensure that the row index is the row in the Grid, not
        // the row index of the data

        assertText("//th[7]", "Indexes (6)");
        assertText("//tr[1]/td[7]", "0,6");
        assertText("//tr[2]/td[7]", "1,6");
    }

    /**
     * TAPESTRY-1416
     */

    @Test
    public void adding_new_columns_to_grid_programattically()
    {
        openLinks("Added Grid Columns Demo", "Title Length");

        assertTextSeries("//th[%d]", 1, "Title", "View", "Title Length", "Dummy");

        // The rendered &nbsp; becomes just a blank string.
        assertTextSeries("//tr[1]/td[%d]", 1, "7", "view", "1", "");
    }

    @Test
    public void inplace_grid()
    {
        openLinks("In-Place Grid Demo");

        String timestamp = getText("lastupdate");

        click("link=2");
        waitForAjaxRequestsToComplete();

        click("link=Album");

        waitForAjaxRequestsToComplete();

        assertEquals(getText("lastupdate"), timestamp,
                "Timestamp should not have changed because updates are in-place.");
    }

    /**
     * TAPESTRY-2502
     */
    @Test
    public void short_grid()
    {
        openLinks("Short Grid");

        for (int i = 0; i < 6; i++)
        {
            String locator = String.format("grid.%d.0", i + 1);
            String expected = String.format("Index #%d", i);

            assertEquals(getTable(locator), expected);
        }

        String count = getEval("window.document.getElementById('grid').rows.length");

        assertEquals(count, "7", "Expected seven rows: the header and six data rows.");
    }

    /**
     * TAPESTRY-1901
     */
    @Test
    public void delete_rows_from_grid()
    {
        openLinks("Delete From Grid", "setup the database", "2");

        for (int i = 6; i <= 10; i++)
            clickAndWait("link=ToDo #" + i);

        // A rather clumsy way to ensure we're back on the first page.

        for (int i = 1; i <= 5; i++)
            assertTextPresent("ToDo #" + i);
    }

    /**
     * TAP5-450
     */
    @Test
    public void rel_nofollow_present_in_sort_links()
    {
        openLinks("Grid Demo", RESET);

        assertAttribute("//a[contains(@href,'columns:sort')]/@rel", "nofollow");
    }

    /**
     * TAP5-2256
     */
    @Test
    public void sorting_inplace_grid_in_a_loop()
    {
        openLinks("In-Place Grid in a Loop Demo", "reset the Grids");

        click("css=.grid1 th[data-grid-property='title'] a");
        waitForAjaxRequestsToComplete();
        click("css=.grid2 th[data-grid-property='album'] a");
        waitForAjaxRequestsToComplete();
        assertAttribute("css=.grid1 th[data-grid-property='title']/@data-grid-column-sort", "ascending");
        assertAttribute("css=.grid2 th[data-grid-property='album']/@data-grid-column-sort", "ascending");
        assertAttribute("css=.grid2 th[data-grid-property='title']/@data-grid-column-sort", "sortable");

    }

    /**
     * TAP5-1658
     */
    @Test
    public void submit_with_context_inside_grid()
    {
        openLinks("Grid with Submit with context");

        clickAndWait("css=tr[data-grid-row='first'] input[type='submit']");
        assertTextPresent("Deleted Bug Juice");

        clickAndWait("css=tr[data-grid-row='last'] input[type='submit']");
        assertTextPresent(" Deleted Studying Stones");

    }
    
    /**
     * TAP5-2437
     */
    @Test
    public void set_grid_current_page_before_first_render()
    {
        openLinks("Grid Early Paging");

        assertTextPresent("Walking On Broken Glass");

    }


}
