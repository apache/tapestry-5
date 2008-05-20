// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class CaseInsensitiveMapTest extends Assert
{
    @Test
    public void basic_get_put_remove()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        String value = "flintstone";

        map.put("fred", value);

        assertEquals(map.toString(), "{fred=flintstone}");

        assertSame(map.get("fred"), value);
        assertSame(map.get("Fred"), value);

        assertSame(map.remove("FRED"), value);

        assertFalse(map.containsKey("fred"));

        assertTrue(map.isEmpty());
    }

    @Test
    public void copy_map_constructor()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Map<String, String> copy = newCaseInsensitiveMap(map);

        assertEquals(copy, map);
    }

    @Test
    public void put_with_different_case_replaces()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        String value = "Murray";

        map.put("Fred", value);

        assertEquals(map.size(), 1);

        assertSame(map.get("fred"), value);

        assertEquals(map.toString(), "{Fred=Murray}");
    }

    @Test
    public void get_with_missing_key_is_null()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        assertNull(map.get("barney"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_with_non_string_key_is_null()
    {
        Map map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        assertNull(map.get(this));
    }

    /**
     * Add a large number of keys which should stress the code that adds and expands values into the map.
     */
    @Test
    public void expansion_of_internal_entry_array()
    {
        Map<String, Integer> map = newCaseInsensitiveMap();

        int COUNT = 2000;

        for (int i = 0; i < COUNT; i++)
        {
            assertNull(map.put("key_" + i, i));
        }

        // Now check that the values are still there.

        for (int i = 0; i < COUNT; i++)
        {
            assertEquals(map.get("KEY_" + i).intValue(), i);
        }

        assertEquals(map.size(), COUNT);
        assertEquals(map.entrySet().size(), COUNT);

        map.clear();

        assertEquals(map.size(), 0);
    }

    @Test
    public void change_value_via_entry_set()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        Map.Entry<String, String> me = map.entrySet().iterator().next();

        String value = "murray";

        me.setValue(value);

        assertSame(map.get("fred"), value);
    }

    @Test(expectedExceptions =
            { ConcurrentModificationException.class })
    public void iterator_fail_fast_after_remove()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Iterator i = map.entrySet().iterator();

        i.next();

        map.remove("betty");

        i.next();
    }

    @Test(expectedExceptions =
            { ConcurrentModificationException.class })
    public void iterator_fail_fast_on_next()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();

        while (i.hasNext())
        {
            if (i.next().getKey().equals("betty")) map.put("pebbles", "flintstone");
        }
    }

    @Test
    public void iterator_may_remove_without_concurrent_exception()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();

        while (i.hasNext())
        {
            if (i.next().getKey().equals("wilma")) i.remove();
        }

        List<String> keys = CollectionFactory.newList(map.keySet());
        Collections.sort(keys);

        assertEquals(keys, Arrays.asList("barney", "betty", "fred"));
    }

    @Test
    public void contains_via_entry_set()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Set<Map.Entry<String, String>> entrySet = map.entrySet();

        assertTrue(entrySet.contains(newMapEntry("fred", "flintstone")));
        assertTrue(entrySet.contains(newMapEntry("Fred", "flintstone")));

        assertFalse(entrySet.contains(newMapEntry("Zaphod", "Beeblebox")));
        assertFalse(entrySet.contains(newMapEntry("fred", "murray")));
    }

    @Test
    public void remove_via_entry_set()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Set<Map.Entry<String, String>> entrySet = map.entrySet();

        assertFalse(entrySet.remove(newMapEntry("Zaphod", "Beeblebox")));
        assertFalse(entrySet.remove(newMapEntry("fred", "murray")));

        assertTrue(entrySet.remove(newMapEntry("Fred", "flintstone")));
    }

    @Test
    public void null_key()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put(null, "NULL");

        assertEquals(map.get(null), "NULL");
    }

    @Test
    public void clear_entry_set_clears_map()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        map.entrySet().clear();

        assertTrue(map.isEmpty());
    }

    @Test(expectedExceptions =
            { NoSuchElementException.class })
    public void next_after_last_entry_is_failure()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");

        Iterator i = map.entrySet().iterator();

        while (i.hasNext())
            i.next();

        i.next();
    }

    @Test
    public void entry_set_iterator_sees_all_keys()
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();
        List<String> keys = CollectionFactory.newList();

        while (i.hasNext())
            keys.add(i.next().getKey());

        Collections.sort(keys);

        assertEquals(keys, Arrays.asList("barney", "betty", "fred", "wilma"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serialize_deserialize() throws Exception
    {
        Map<String, String> map = newCaseInsensitiveMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");
        map.put("wilma", "flinstone");
        map.put("betty", "rubble");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        Map<String, String> copy = (Map<String, String>) ois.readObject();

        assertEquals(copy, map);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map.Entry<K, V> newMapEntry(final K key, final V value)
    {
        return new Map.Entry()
        {

            public Object getKey()
            {
                return key;
            }

            public Object getValue()
            {
                return value;
            }

            public Object setValue(Object value)
            {
                throw new UnsupportedOperationException();
            }

        };
    }
}
