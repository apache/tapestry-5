// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.ioc.test.TestBase;
import org.testng.annotations.Test;

/**
 * 
 */
public class InfrastructureContributionTest extends TestBase
{
    @Test
    public void default_for_mode()
    {
        InfrastructureContribution ic = new InfrastructureContribution("fred", this);

        assertEquals("fred", ic.getName());
        assertEquals("", ic.getMode());
        assertSame(ic.getObject(), this);
    }

    @Test
    public void specific_mode()
    {
        InfrastructureContribution ic = new InfrastructureContribution("fred", "mode", this);

        assertEquals("fred", ic.getName());
        assertEquals("mode", ic.getMode());
        assertSame(ic.getObject(), this);
    }

    @Test
    public void to_string()
    {
        InfrastructureContribution ic = new InfrastructureContribution("fred", "FRED");

        assertEquals(ic.toString(), "<InfrastructureContribution: fred FRED>");

        ic = new InfrastructureContribution("fred", "servlet", "FRED");

        assertEquals(ic.toString(), "<InfrastructureContribution: fred mode:servlet FRED>");
    }
}
