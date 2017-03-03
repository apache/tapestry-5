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

import org.apache.tapestry5.corelib.components.Palette;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Palette} component.
 */
public class PaletteTests extends App1TestCase
{

    public static final String AVAILABLE_OPTIONS = "css=.palette-available select";

    public static final String SELECTED_OPTIONS = "css=.palette-selected select";

    public static final String SELECT_BUTTON = "css=.palette [data-action=select]";

    public static final String DESELECT_BUTTON = "css=.palette [data-action=deselect]";

    public static final String MOVE_UP_BUTTON = "css=.palette [data-action=move-up]";

    public static final String MOVE_DOWN_BUTTON = "css=.palette [data-action=move-down]";

    @Test
    public void palette_component()
    {
        openLinks("Palette Demo", "Reset Page State");

        assertText("css=.palette-available .palette-title",
                "Languages Offered");
        assertText("css=.palette-selected .palette-title",
                "Selected Languages");

        addSelection(AVAILABLE_OPTIONS, "label=Haskell");
        addSelection(AVAILABLE_OPTIONS, "label=Javascript");
        click(SELECT_BUTTON);

        // What a listener on the events.palette.willChange event would see in memo.selectdValues:
        assertText("id=event-selection", "[\"HASKELL\",\"JAVASCRIPT\"]");


        clickAndWait(SUBMIT);

        assertText("id=selected-languages", "[HASKELL, JAVASCRIPT]");

        addSelection(SELECTED_OPTIONS, "label=Javascript");

        click(DESELECT_BUTTON);

        addSelection(AVAILABLE_OPTIONS, "label=Perl");
        removeSelection(AVAILABLE_OPTIONS, "label=Javascript");
        addSelection(AVAILABLE_OPTIONS, "label=Erlang");
        addSelection(AVAILABLE_OPTIONS, "label=Java");
        addSelection(AVAILABLE_OPTIONS, "label=Lisp");
        addSelection(AVAILABLE_OPTIONS, "label=Ml");
        addSelection(AVAILABLE_OPTIONS, "label=Python");
        addSelection(AVAILABLE_OPTIONS, "label=Ruby");

        click(SELECT_BUTTON);

        clickAndWait(SUBMIT);

        assertText("id=selected-languages", "[ERLANG, HASKELL, JAVA, LISP, ML, PERL, PYTHON, RUBY]");

        check("reorder");

        clickAndWait(SUBMIT);

        addSelection(SELECTED_OPTIONS, "label=Ruby");

        for (int i = 0; i < 6; i++)
        {
            click(MOVE_UP_BUTTON);
        }

        removeSelection(SELECTED_OPTIONS, "label=Ruby");
        addSelection(SELECTED_OPTIONS, "label=Perl");

        click(MOVE_DOWN_BUTTON);

        clickAndWait(SUBMIT);

        assertText("id=selected-languages", "[ERLANG, RUBY, HASKELL, JAVA, LISP, ML, PYTHON, PERL]");
    }

    /**
     * TAP5-298
     */
    @Test
    public void palette_component_disabled_options()
    {
        openLinks("Palette Demo", "Reset Page State");

        /*
         * force of the options to be disabled rather than creating the model
         * with it disabled in the page.
         * it is possible to get into this state by creating a model with
         * disabled options.
         */
        WebElement option = webDriver.findElement(convertLocator(AVAILABLE_OPTIONS+" option"));
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("arguments[0].disabled = 'disabled'", option);

        // causes an error in the js console but does not throw an exception
        // here. optimally, this would make the test case fail.
        doubleClick(AVAILABLE_OPTIONS + " option");
    }

    @Test
    public void palette_component_client_validation()
    {
        openLinks("Palette Demo", "Reset Page State");

        click(SUBMIT);

        assertTextPresent("You must provide a value for Languages.");
    }
}
