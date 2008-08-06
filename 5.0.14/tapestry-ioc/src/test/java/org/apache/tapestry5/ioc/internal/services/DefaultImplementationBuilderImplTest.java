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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.DefaultImplementationBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultImplementationBuilderImplTest extends IOCInternalTestCase
{
    private DefaultImplementationBuilder builder;

    @BeforeClass
    public void setup_builder()
    {
        builder = getService("DefaultImplementationBuilder", DefaultImplementationBuilder.class);
    }

    @AfterClass
    public void cleanup_builder()
    {
        builder = null;
    }

    @Test
    public void simple_interface()
    {
        Runnable r = builder.createDefaultImplementation(Runnable.class);

        r.run();

        assertEquals(r.toString(), "<NoOp java.lang.Runnable>");
    }

    public interface ToString
    {
        String toString();
    }

    @Test
    public void interface_has_toString()
    {
        ToString ts = builder.createDefaultImplementation(ToString.class);

        assertNull(ts.toString());
    }

    @Test
    public void instances_are_cached()
    {
        Runnable r1 = null;
        Runnable r2 = null;

        // With tests in parallel, there's a harmless race condition that can cause r1 != r2
        // for one pass, so we give it a second chance to prove itself.

        for (int i = 0; i < 2; i++)
        {
            r1 = builder.createDefaultImplementation(Runnable.class);
            r2 = builder.createDefaultImplementation(Runnable.class);

            if (r1 == r2) break;
        }

        assertSame(r2, r1);
    }
}
