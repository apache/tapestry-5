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

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class BlockTests extends TapestryCoreTestCase
{
    // Test for TAP5-1600
    @Test
    void components_inside_blocks_are_visible_as_embedded() {

        openLinks "Component Inside Block Demo"

        // If TAP5-1600 was valid (it neve was), we'd get an exception page,
        // not the actual page.

        assertText "testtitle", "Component Inside Block"
    }
}
