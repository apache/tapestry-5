// Copyright 2009, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Palette} component.
 */
public class PaletteTests extends TapestryCoreTestCase
{
    @Test
    public void palette_component()
    {
        openLinks("Palette Demo", "reset");

        assertText("//div[@class='t-palette-available']/div[@class='t-palette-title']",
                "Languages Offered");
        assertText("//div[@class='t-palette-selected']/div[@class='t-palette-title']",
                "Selected Languages");

        addSelection("languages-avail", "label=Haskell");
        addSelection("languages-avail", "label=Javascript");
        click("languages-select");

        clickAndWait(SUBMIT);
        assertTextPresent("Selected Languages: [HASKELL, JAVASCRIPT]");

        addSelection("languages", "label=Javascript");
        click("languages-deselect");

        addSelection("languages-avail", "label=Perl");
        removeSelection("languages-avail", "label=Javascript");
        addSelection("languages-avail", "label=Erlang");
        addSelection("languages-avail", "label=Java");
        addSelection("languages-avail", "label=Lisp");
        addSelection("languages-avail", "label=Ml");
        addSelection("languages-avail", "label=Python");
        addSelection("languages-avail", "label=Ruby");

        click("languages-select");

        clickAndWait(SUBMIT);

        assertTextPresent("[ERLANG, HASKELL, JAVA, LISP, ML, PERL, PYTHON, RUBY]");

        check("reorder");
        clickAndWait(SUBMIT);

        assertText("//div[@class='t-palette-selected']/div[@class='t-palette-title']",
                "Selected / Ranked Languages");

        addSelection("languages", "label=Ruby");

        for (int i = 0; i < 6; i++)
            click("languages-up");

        removeSelection("languages", "label=Ruby");
        addSelection("languages", "label=Perl");

        click("languages-down");

        clickAndWait(SUBMIT);

        assertTextPresent("[ERLANG, RUBY, HASKELL, JAVA, LISP, ML, PYTHON, PERL]");
    }

    /**
     * TAP5-298
     */
    @Test
    public void palette_component_disabled_options()
    {
        openLinks("Palette Demo", "reset");

        /*
         * force of the options to be disabled rather than creating the model
         * with it disabled in the page.
         * it is possible to get into this state by creating a model with
         * disabled options.
         */
        getEval("this.browserbot.findElement('//select[@id=\"languages-avail\"]/option[1]').disabled = 'disabled';");

        // causes an error in the js console but does not throw an exception
        // here. optimally, this would make the test case fail.
        doubleClick("//select[@id=\"languages-avail\"]/option[1]");
    }

    @Test
    public void palette_component_client_validation()
    {
        openLinks("Palette Demo", "reset");

        click(SUBMIT);

        assertBubbleMessage("languages", "You must provide a value for Languages.");
    }
}
