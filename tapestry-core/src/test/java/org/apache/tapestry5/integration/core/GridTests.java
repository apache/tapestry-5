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

import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Grid} component.
 */
public class GridTests extends TapestryCoreTestCase
{
    /**
     * Basic Grid rendering, with a column render override. Also tests sorting.
     */
    @Test
    public void basic_grid()
    {
        clickThru("Grid Demo");

        // "Sort Rating" via the header cell override (TAPESTRY-2081)

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count",
                "Sort Rating");

        // Strange: I thought tr[1] was the header row ???

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip",
                "Electronica", "4", "-");

        // Here were checking that the page splits are correct

        clickAndWait("link=3");

        // Last on page 3:
        assertText("//tr[25]/td[1]", "Blood Red River");

        clickAndWait("link=4");
        assertText("//tr[1]/td[1]", "Devil Song");

        clickAndWait("link=7");
        clickAndWait("link=10");

        // Here's one with a customized rating cell

        assertTextSeries("//tr[25]/td[%d]", 1, "Smoked",
                "London (Original Motion Picture Soundtrack)", "The Crystal Method", "Soundtrack",
                "30", "****");

        clickAndWait("link=69");

        assertText("//tr[22]/td[1]", "radioioAmbient");

        // Sort ascending (and we're on the last page, with the highest
        // ratings).

        clickAndWait("link=Sort Rating");

        assertTextSeries("//tr[22]/td[%d]", 1, "Mona Lisa Overdrive", "Labyrinth", "Juno Reactor",
                "Dance", "31", "*****");

        // Toggle to sort descending

        clickAndWait("link=Sort Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Hey Blondie", "Out from Out Where");

        clickAndWait("link=Title");

        // The lack of a leading slash indicates that the path was optimized,
        // see TAPESTRY-1502

        assertAttribute("//img[@class='t-sort-icon']/@src",
                "/assets/tapestry/UNKNOWN/corelib/components/sort-asc.png");
        assertAttribute("//img[@class='t-sort-icon']/@alt", "[Asc]");

        clickAndWait("link=1");

        assertText("//tr[1]/td[1]", "(untitled hidden track)");

        clickAndWait("link=Title");

        assertAttribute("//img[@class='t-sort-icon']/@src",
                "/assets/tapestry/UNKNOWN/corelib/components/sort-desc.png");
        assertAttribute("//img[@class='t-sort-icon']/@alt", "[Desc]");

        clickAndWait("link=reset the Grid");

        // Back to where we started.

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip",
                "Electronica", "4", "-");
    }

    @Test
    public void grid_remove_reorder()
    {
        clickThru("Grid Remove/Reorder Demo");

        assertTextSeries("//th[%d]", 1, "Rating", "Title", "Album", "Artist", "Genre");
    }

    @Test
    public void grid_set() throws Exception
    {
        clickThru("Grid Set Demo");

        assertFalse(isTextPresent("Exception"));

        // Also check for TAPESTRY-2228

        assertAttribute("//table/@informal", "supported");
    }

    @Test
    public void grid_from_explicit_interface_model()
    {
        clickThru("SimpleTrack Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "-");
    }

    @Test
    public void grid_enum_display()
    {
        clickThru("Grid Enum Demo", "reset");

        assertTextSeries("//tr[1]/td[%d]", 1, "End World Hunger", "Medium");
        assertTextSeries("//tr[2]/td[%d]", 1, "Develop Faster-Than-Light Travel", "Ultra Important");
        assertTextSeries("//tr[3]/td[%d]", 1, "Cure Common Cold", "Low");
    }

    @Test
    public void null_grid() throws Exception
    {
        clickThru("Null Grid");

        assertTextPresent("There is no data to display.");
    }
}
