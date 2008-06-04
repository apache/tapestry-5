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

package org.apache.tapestry5.internal.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;

public class IntegerRangeTest extends Assert
{
    @Test
    public void start_less_than_finish()
    {
        IntegerRange r = new IntegerRange(1, 3);

        assertEquals(r.toString(), "1..3");

        assertEquals(r.getStart(), 1);
        assertEquals(r.getFinish(), 3);

        Iterator<Integer> i = r.iterator();

        assertEquals(i.next().intValue(), 1);
        assertEquals(i.next().intValue(), 2);

        assertTrue(i.hasNext());

        assertEquals(i.next().intValue(), 3);

        assertFalse(i.hasNext());

        try
        {
            i.next();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
        }
    }

    @Test
    public void start_same_as_finish()
    {
        IntegerRange r = new IntegerRange(3, 3);

        Iterator<Integer> i = r.iterator();

        assertTrue(i.hasNext());

        assertEquals(i.next().intValue(), 3);

        assertFalse(i.hasNext());
    }

    @Test
    public void finish_less_than_start()
    {
        IntegerRange r = new IntegerRange(3, 1);

        assertEquals(r.toString(), "3..1");

        Iterator<Integer> i = r.iterator();

        assertEquals(i.next().intValue(), 3);
        assertEquals(i.next().intValue(), 2);

        assertTrue(i.hasNext());

        assertEquals(i.next().intValue(), 1);

        assertFalse(i.hasNext());

        try
        {
            i.next();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
        }
    }

    @Test
    public void hash_code_and_equals()
    {
        IntegerRange r1 = new IntegerRange(1, 100);
        IntegerRange r2 = new IntegerRange(1, 100);
        IntegerRange r3 = new IntegerRange(1, 10);

        assertEquals(r1.hashCode(), r2.hashCode());
        assertFalse(r1.hashCode() == r3.hashCode());

        assertTrue(r1.equals(r1));
        assertEquals(r1, r2);

        assertFalse(r1.equals(r3));
        assertFalse(r1.equals(this));
        assertFalse(r1.equals(null));
        assertFalse(r1.equals(new IntegerRange(3, 30)));
    }

    @Test
    public void iterator_remove_not_supported()
    {
        IntegerRange r = new IntegerRange(1, 3);

        Iterator<Integer> i = r.iterator();

        assertEquals(i.next(), new Integer(1));

        try
        {
            i.remove();
            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected, ignored.
        }

        assertEquals(i.next(), new Integer(2));
    }

    private final void unreachable()
    {
        throw new AssertionError("This code should be unreachable.");
    }
}
