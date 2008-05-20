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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.util.Random;

public class LocationImplTest extends IOCTestCase
{
    private final Random random = new Random();

    private final Resource resource = new ClasspathResource("/foo/Bar.xml");

    @Test
    public void all_three_parameters()
    {

        int line = random.nextInt();
        int column = random.nextInt();

        Location l = new LocationImpl(resource, line, column);

        assertSame(l.getResource(), resource);
        assertEquals(l.getLine(), line);
        assertEquals(l.getColumn(), column);

        assertEquals(l.toString(), String.format("%s, line %d, column %d", resource, line, column));
    }

    @Test
    public void unknown_column()
    {
        int line = random.nextInt();

        Location l = new LocationImpl(resource, line);

        assertSame(l.getResource(), resource);
        assertEquals(l.getLine(), line);
        assertEquals(l.getColumn(), -1);

        assertEquals(l.toString(), String.format("%s, line %d", resource, line));
    }

    @Test
    public void unknown_line_and_column()
    {
        Location l = new LocationImpl(resource);

        assertSame(l.getResource(), resource);
        assertEquals(l.getLine(), -1);
        assertEquals(l.getColumn(), -1);

        assertEquals(l.toString(), resource.toString());
    }

    @Test
    public void equality()
    {
        Location l1 = new LocationImpl(resource, 22, 7);
        Location l2 = new LocationImpl(resource, 22, 7);
        Location l3 = new LocationImpl(null, 22, 7);
        Location l4 = new LocationImpl(resource, 99, 7);
        Location l5 = new LocationImpl(resource, 22, 99);
        Location l6 = new LocationImpl(new ClasspathResource("/baz/Biff.txt"), 22, 7);

        assertEquals(l1, l1);
        assertFalse(l1.equals(null));

        assertEquals(l1, l2);
        assertEquals(l2.hashCode(), l1.hashCode());

        assertFalse(l3.equals(l1));
        assertFalse(l3.hashCode() == l1.hashCode());

        assertFalse(l4.equals(l1));
        assertFalse(l4.hashCode() == l1.hashCode());

        assertFalse(l5.equals(l1));
        assertFalse(l5.hashCode() == l1.hashCode());

        assertFalse(l6.equals(l1));
        assertFalse(l6.hashCode() == l1.hashCode());
    }
}
