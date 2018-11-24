// Copyright 2011-2013 The Apache Software Foundation
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

import org.testng.annotations.Test

class TreeTests extends App1TestCase {

    @Test
    void basics() {
        openLinks "Tree Component Demo", "Clear Expansions"

        //Click on Games
        click "//div[@class='tree-container test-hook']/ul/li[2]/span[@class='tree-icon']"

        sleep 25 // See if that helps with the intermittent test suite failures on the CI server

        waitForAjaxRequestsToComplete PAGE_LOAD_TIMEOUT

        assertTextPresent "Board Games"

        //Click on Board Games
        click "//div[@class='tree-container test-hook']/ul/li[2]/ul/li/span[@class='tree-icon']"

        //Assert that the leafs are displayed
        waitForAjaxRequestsToComplete PAGE_LOAD_TIMEOUT

        clickAndWait "link=Redraw"

        assertTextPresent "Settlers of Catan", "Agricola"
    }

    @Test
    void select_node() {
        openLinks "Tree Component Selection Demo", "Clear All"

        click "css=.tree-container span.tree-icon"

        // Need a short sleep
        sleep 250

        // Since there's no simple way to build the CSS select we need.
        // waitForCssSelectorToAppear "li.leaf-node > span"

        assertTextPresent "Oscar", "Gromit", "Max", "Roger", "Cooper"

        // Click the first selectable node, probably Oscar

        click "css=[data-tree-node-selection-enabled] li.leaf-node > span.tree-label"

        waitForCssSelectorToAppear "span.selected-leaf-node"

        clickAndWait "link=Redraw"

        // Make sure it is still there after a redraw

        waitForCssSelectorToAppear "span.selected-leaf-node"
    }
}
