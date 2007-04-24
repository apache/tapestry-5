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

package org.apache.tapestry.ioc.internal.util;

import java.util.Random;

import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class LocationImplTest extends IOCTestCase
{
    private final Random _random = new Random();

    private final Resource _resource = new ClasspathResource("/foo/Bar.xml");

    @Test
    public void all_three_parameters()
    {

        int line = _random.nextInt();
        int column = _random.nextInt();

        Location l = new LocationImpl(_resource, line, column);

        assertSame(l.getResource(), _resource);
        assertEquals(l.getLine(), line);
        assertEquals(l.getColumn(), column);

        assertEquals(l.toString(), String.format("%s, line %d, column %d", _resource, line, column));
    }

    @Test
    public void unknown_column()
    {
        int line = _random.nextInt();

        Location l = new LocationImpl(_resource, line);

        assertSame(l.getResource(), _resource);
        assertEquals(l.getLine(), line);
        assertEquals(l.getColumn(), -1);

        assertEquals(l.toString(), String.format("%s, line %d", _resource, line));
    }

    @Test
    public void unknown_line_and_column()
    {

        Location l = new LocationImpl(_resource);

        assertSame(l.getResource(), _resource);
        assertEquals(l.getLine(), -1);
        assertEquals(l.getColumn(), -1);

        assertEquals(l.toString(), _resource.toString());
    }
}
