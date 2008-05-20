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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import static java.util.Arrays.asList;

public class CollectionFactoryTest extends Assert
{

    @Test
    public void new_map()
    {
        Map<String, Class> map = newMap();

        assertTrue(map instanceof HashMap);
    }

    @Test
    public void copy_map()
    {
        Map<String, Class> map = newMap();

        map.put("this", CollectionFactoryTest.class);

        Map<String, Class> copy = CollectionFactory.newMap(map);

        assertEquals(copy, map);

        map.put("other", Map.class);

        assertFalse(copy.equals(map));
    }

    @Test
    public void new_set()
    {
        Set<String> set = newSet();

        assertTrue(set instanceof HashSet);
    }

    @Test
    public void copy_set()
    {
        List<String> start = asList("fred", "barney");

        Set<String> set = newSet(start);

        assertEquals(set.size(), 2);
        assertTrue(set.contains("fred"));
        assertTrue(set.contains("barney"));
    }

    @Test
    public void set_from_varargs()
    {
        Set<String> set = newSet("fred", "barney");

        assertEquals(set.size(), 2);
        assertTrue(set.contains("fred"));
        assertTrue(set.contains("barney"));
    }

    @Test
    public void new_list()
    {
        List<String> list = newList();

        assertTrue(list instanceof ArrayList);
    }

    @Test
    public void new_list_copy()
    {
        List<String> start = Arrays.asList("Fred", "Barney", "Wilma");
        List<String> copy = newList(start);

        assertNotSame(copy, start);
        assertEquals(copy, start);
    }

    @Test
    public void new_list_from_elements()
    {
        List<String> list = newList("Fred", "Barney");

        assertEquals(list.size(), 2);
        assertEquals(list.get(0), "Fred");
        assertEquals(list.get(1), "Barney");
    }

    private static final int THREAD_COUNT = 20;

    @Test
    public void new_threadsafe_list() throws Exception
    {
        final List<String> threadNames = CollectionFactory.newThreadSafeList();

        List<Thread> threads = CollectionFactory.newList();

        Runnable r = new Runnable()
        {
            public void run()
            {
                String name = Thread.currentThread().getName();
                threadNames.add(name);
            }
        };

        for (int i = 0; i < THREAD_COUNT; i++)
        {
            Thread t = new Thread(r);
            threads.add(t);
        }

        // Start all the threads at the same time.

        for (Thread t : threads)
        {
            t.start();
        }

        // Wait for all threads to complete

        for (Thread t : threads)
        {
            t.join();
        }

        // Make sure they all executed. If the list was not thread safe, highly unlikely this
        // would work.

        assertEquals(threadNames.size(), THREAD_COUNT);
    }
}
