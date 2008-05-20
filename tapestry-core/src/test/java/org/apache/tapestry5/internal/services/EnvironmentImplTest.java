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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EnvironmentImplTest extends TapestryTestCase
{
    @Test
    public void peek_when_empty_returns_null()
    {
        Environment e = new EnvironmentImpl();

        assertNull(e.peek(Runnable.class));
        assertNull(e.peek(Map.class));
    }

    @Test
    public void push_and_pop()
    {
        Environment e = new EnvironmentImpl();
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        assertNull(e.push(Runnable.class, r1));

        assertSame(r1, e.peek(Runnable.class));

        assertSame(r1, e.push(Runnable.class, r2));

        assertSame(r2, e.peek(Runnable.class));

        assertSame(r2, e.pop(Runnable.class));
        assertSame(r1, e.pop(Runnable.class));

        verify();
    }

    @Test
    public void clear()
    {
        Environment e = new EnvironmentImpl();
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        e.push(Runnable.class, r1);
        e.push(Runnable.class, r2);

        e.clear();

        assertNull(e.peek(Runnable.class));

        verify();
    }

    @Test
    public void pop_when_empty_is_error()
    {
        Environment e = new EnvironmentImpl();

        try
        {
            e.pop(Runnable.class);
            unreachable();
        }
        catch (NoSuchElementException ex)
        {
        }
    }

    @Test
    public void peek_required_when_available()
    {
        Environment e = new EnvironmentImpl();
        Location l = mockLocation();

        replay();

        e.push(Location.class, l);

        assertSame(l, e.peekRequired(Location.class));

        verify();
    }

    @Test
    public void peek_required_without_value_is_error()
    {
        Environment e = new EnvironmentImpl();
        Location l = mockLocation();
        Component c = mockComponent();

        replay();

        e.push(Location.class, l);
        e.push(Component.class, c);

        try
        {
            e.peekRequired(List.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No object of type java.util.List is available from the Environment.  Available types are org.apache.tapestry5.ioc.Location, org.apache.tapestry5.runtime.Component.");
        }

        verify();
    }
}
