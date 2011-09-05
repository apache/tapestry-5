// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.test.SeleniumTestCase
import org.testng.annotations.Test

class TreeTests extends SeleniumTestCase
{
    @Test
    void basics()
    {

        openBaseURL()

        clickAndWait "link=Tree Component Demo"

        clickAndWait "link=clear expansions"

        //Click on Games
        click "//div[@class='t-tree-container test-hook']/ul/li[2]/span[@class='t-tree-icon']"

        sleep 25 // See if that helps with the intermittent test suite failures on the CI server

        waitForAjaxRequestsToComplete PAGE_LOAD_TIMEOUT

        assertTextPresent "Board Games"

        //Click on Board Games
        click "//div[@class='t-tree-container test-hook']/ul/li[2]/ul/li/span[@class='t-tree-icon']"

        //Assert the leafs are displayed
        waitForAjaxRequestsToComplete PAGE_LOAD_TIMEOUT

        clickAndWait "link=Redraw"

        assertTextPresent "Settlers of Catan", "Agricola"
    }

    @Test
    void select_node()
    {

        openBaseURL()

        clickAndWait "link=Tree Component Selection Demo"

        clickAndWait "link=clear all"

        click "//span[@class='t-tree-icon']"

        waitForCSSSelectedElementToAppear "span.t-leaf-node"

        assertTextPresent "Oscar", "Gromit", "Max", "Roger", "Cooper"

        // Click the first selectable node, probably Oscar
        click "css=span.t-selectable"

        waitForCSSSelectedElementToAppear "span.t-selected-leaf-node-label"

        clickAndWait "link=Redraw"

        // Make sure it is still there after a redraw

        waitForCSSSelectedElementToAppear "span.t-selected-leaf-node-label"
    }
}
