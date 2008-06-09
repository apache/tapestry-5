// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class AliasContributionTest extends TapestryTestCase
{
    @Test
    public void default_for_mode()
    {
        Runnable r = mockRunnable();

        replay();

        AliasContribution contribution = AliasContribution.create(Runnable.class, r);

        assertSame(contribution.getContributionType(), Runnable.class);
        assertEquals(contribution.getMode(), "");
        assertSame(contribution.getObject(), r);

        verify();
    }

    @Test
    public void specific_mode()
    {
        Runnable r = mockRunnable();

        replay();

        AliasContribution contribution = new AliasContribution<Runnable>(Runnable.class, "mode", r);

        assertEquals(contribution.getContributionType(), Runnable.class);
        assertEquals(contribution.getMode(), "mode");
        assertSame(contribution.getObject(), r);

        verify();
    }

    @Test
    public void to_string()
    {
        AliasContribution contribution = AliasContribution.create(String.class, "FRED");

        assertEquals(contribution.toString(), "<AliasContribution: java.lang.String FRED>");

        contribution = new AliasContribution<String>(String.class, "servlet", "FRED");

        assertEquals(
                contribution.toString(),
                "<AliasContribution: java.lang.String mode:servlet FRED>");
    }
}
