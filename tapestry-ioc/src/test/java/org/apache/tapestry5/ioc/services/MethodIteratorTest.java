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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

public class MethodIteratorTest extends IOCTestCase
{
    static interface Play extends Runnable
    {
        public void jump();
    }

    static interface Runnable2
    {
        public void run();
    }

    static interface Runnable3 extends Runnable, Runnable2
    {
    }

    static interface ToString
    {
        public String toString();
    }

    static interface Openable
    {
        public void open();
    }

    static interface OpenableWithError
    {
        public void open() throws IOException;
    }

    static interface CombinedOpeneable extends Openable, OpenableWithError
    {
    }

    @Test
    public void simple_interface()
    {
        MethodIterator mi = new MethodIterator(Runnable.class);

        assertTrue(mi.hasNext());

        MethodSignature actual = mi.next();

        assertEquals(new MethodSignature(void.class, "run", null, null), actual);

        assertFalse(mi.hasNext());

        try
        {
            mi.next();
        }
        catch (NoSuchElementException ex)
        {
            //
        }

        assertEquals(false, mi.getToString());
    }

    @Test
    public void inherited_methods_from_super_interface()
    {
        MethodIterator mi = new MethodIterator(Play.class);

        assertTrue(mi.hasNext());

        // Problematic because the order in which they are returned is
        // JDK specific and not defined! Perhaps we should sort by alpha?

        MethodSignature actual = mi.next();

        assertEquals(new MethodSignature(void.class, "jump", null, null), actual);

        assertTrue(mi.hasNext());

        actual = mi.next();

        assertEquals(new MethodSignature(void.class, "run", null, null), actual);

        assertFalse(mi.hasNext());

        assertEquals(false, mi.getToString());
    }

    @Test
    public void duplicate_methods_filtered_out()
    {
        MethodIterator mi = new MethodIterator(Runnable3.class);

        MethodSignature actual = mi.next();

        assertEquals(new MethodSignature(void.class, "run", null, null), actual);

        assertEquals(false, mi.getToString());
    }

    @Test
    public void to_string_method_identified()
    {
        MethodIterator mi = new MethodIterator(ToString.class);

        // Show that this is known immediately.

        assertEquals(true, mi.getToString());

        MethodSignature actual = mi.next();

        assertEquals(new MethodSignature(String.class, "toString", null, null), actual);

    }

    @Test
    public void inherited_methods_filtered_if_less_specific()
    {
        MethodIterator mi = new MethodIterator(CombinedOpeneable.class);

        MethodSignature actual = mi.next();

        assertEquals(new MethodSignature(void.class, "open", null, new Class[] { IOException.class }), actual);

        assertEquals(false, mi.hasNext());
    }
}
