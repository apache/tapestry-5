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

import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests related to the {@link Zone} component.
 */
public class ZoneTests extends TapestryCoreTestCase
{
    /**
     * TAP5-138
     */
    @Test
    public void select_zone()
    {
        clickThru("Select Zone Demo");

        type("carMaker", "BMW");

        waitForElementToAppear("carModelContainer");

        click(SUBMIT);

        String condition = String.format("window.$$(\"%s\")", "t-error-popup");

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);

        assertText(String.format("//div[@class='%s']/span", "t-error-popup"),
                "You must provide a value for Car Model.");

        type("carModel", "7 Series");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: BMW");

        assertTextPresent("Car Model: 7 Series");

        waitForElementToDisappear("carModelContainer");

        type("carMaker", "MERCEDES");

        waitForElementToAppear("carModelContainer");

        type("carModel", "E-Class");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: MERCEDES");

        assertTextPresent("Car Model: E-Class");
    }

    @Test
    public void zone_updates()
    {
        clickThru("Zone Demo");

        assertTextPresent("No name has been selected.");

        // Hate doing this, but selecting by the text isn't working, perhaps
        // because of the
        // HTML entities.
        click("select_0");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.

        // assertTextPresent("Selected: Mr. &lt;Roboto&gt;");

        click("link=Direct JSON response");
    }
}
