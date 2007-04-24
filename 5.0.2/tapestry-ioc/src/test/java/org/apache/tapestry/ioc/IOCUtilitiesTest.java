// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc;

import static org.apache.tapestry.ioc.IOCUtilities.qualifySimpleIdList;
import static org.apache.tapestry.ioc.IOCUtilities.toQualifiedId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.ioc.IOCUtilities}.
 * 
 * 
 */
public class IOCUtilitiesTest
{

    @Test
    public void qualify_single_simple_id()
    {
        assertEquals(toQualifiedId("module", "Fred"), "module.Fred");
    }

    @Test
    public void qualify_single_qualified_id()
    {
        assertEquals(toQualifiedId("zaphod", "module.Fred"), "module.Fred");
    }

    @Test
    public void qualify_id_list()
    {
        assertEquals(
                qualifySimpleIdList("module", "Fred,othermodule.Barney,module.Wilma"),
                "module.Fred,othermodule.Barney,module.Wilma");
    }

    @Test
    public void qualify_list_as_star()
    {
        assertEquals(qualifySimpleIdList("module", "*"), "*");
    }

    @Test
    public void qualify_blank_or_null_list()
    {
        assertNull(qualifySimpleIdList("module", null));

        assertEquals(qualifySimpleIdList("module", ""), "");
    }
}
