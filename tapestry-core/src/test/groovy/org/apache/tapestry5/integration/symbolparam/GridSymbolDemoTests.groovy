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

package org.apache.tapestry5.integration.symbolparam

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

/**
 * Tests for the Grid's symbol parameter support added in 5.3.
 */
class GridSymbolDemoTests extends TapestryCoreTestCase
{
    @Test
    void grid_default_symbol_override()
    {
        openLinks "GridSymbol"

        clickAndWait "link=4"

        // Using the XPath selectors was a bit flakey, so maybe css is better.
        assertText("css=tr.t-first td.me", "6");
        assertText("css=tr.t-first td.odd", "false");
        assertText("css=tr.t-last td.me", "7");
        assertText("css=tr.t-first td.odd", "false");
    }
}
