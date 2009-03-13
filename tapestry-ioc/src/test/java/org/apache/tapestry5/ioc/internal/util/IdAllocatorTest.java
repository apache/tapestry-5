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

package org.apache.tapestry5.ioc.internal.util;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class IdAllocatorTest extends Assert
{

    @Test
    public void simple()
    {
        IdAllocator a = new IdAllocator();
        List<String> ids = newList();

        assertFalse(a.isAllocated("name"));

        assertEquals(a.allocateId("name"), "name");
        assertTrue(a.isAllocated("name"));

        ids.add("name");

        for (int i = 0; i < 10; i++)
        {

            String expected = "name_" + i;

            assertFalse(a.isAllocated(expected));

            String nextId = a.allocateId("name");

            assertTrue(a.isAllocated(expected));

            assertEquals(nextId, expected);

            ids.add(nextId);
        }

        assertEquals(a.getAllocatedIds(), ids);
    }

    @Test
    public void simple_with_namespace()
    {
        IdAllocator a = new IdAllocator("_NS");

        assertEquals(a.allocateId("name"), "name_NS");

        for (int i = 0; i < 10; i++)
            assertEquals(a.allocateId("name"), "name_NS_" + i);

        // This is current behavior, but is probably something
        // that could be improved.

        assertEquals(a.allocateId("foo_NS"), "foo_NS_NS");
        assertEquals(a.allocateId("foo_NS"), "foo_NS_NS_0");
    }

    @Test
    public void degenerate()
    {
        IdAllocator a = new IdAllocator();

        assertEquals(a.allocateId("d_1"), "d_1");

        assertEquals(a.allocateId("d"), "d");
        assertEquals(a.allocateId("d"), "d_0");
        assertEquals(a.allocateId("d"), "d_2");

        assertEquals(a.allocateId("d"), "d_3");
        assertEquals(a.allocateId("d_1"), "d_1_0");
    }

    @Test
    public void degenerate_with_namespace()
    {
        IdAllocator a = new IdAllocator("_NS");

        assertEquals(a.allocateId("d_1"), "d_1_NS");

        assertEquals(a.allocateId("d"), "d_NS");
        assertEquals(a.allocateId("d"), "d_NS_0");
        assertEquals(a.allocateId("d"), "d_NS_1");
        assertEquals(a.allocateId("d"), "d_NS_2");
        assertEquals(a.allocateId("d"), "d_NS_3");

        assertEquals(a.allocateId("d_1"), "d_1_NS_0");

        // This is very degenerate, and maybe something that needs fixing.

        assertEquals(a.allocateId("d_1_NS"), "d_1_NS_NS");
    }

    @Test
    public void clear()
    {
        IdAllocator a = new IdAllocator();

        assertEquals(a.allocateId("foo"), "foo");
        assertEquals(a.allocateId("foo_0"), "foo_0");

        a.clear();

        assertEquals(a.allocateId("foo"), "foo");
        assertEquals(a.allocateId("foo_0"), "foo_0");
    }

    @Test
    public void clone_test()
    {
        IdAllocator a = new IdAllocator();

        assertEquals(a.allocateId("foo"), "foo");
        assertEquals(a.allocateId("foo_0"), "foo_0");
        assertEquals(a.allocateId("foo"), "foo_1");

        IdAllocator b = a.clone();

        // After making a clone, parallel operations should return the same results.
        // If anything under the covers was shared, then parallel operations would
        // interfere with each other.

        assertEquals(b.allocateId("bar"), a.allocateId("bar"));
        assertEquals(b.allocateId("foo"), a.allocateId("foo"));
        assertEquals(b.allocateId("foo_0"), a.allocateId("foo_0"));

    }

}
