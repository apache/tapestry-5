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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.InternalUtils.toList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.Locatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InternalUtilsTest extends IOCTestCase
{
    @Test
    public void method_as_string_no_args() throws Exception
    {

        Method m = Object.class.getMethod("toString");

        assertEquals(InternalUtils.asString(m), "java.lang.Object.toString()");
    }

    @Test
    public void method_as_string_with_args() throws Exception
    {
        Method m = Collections.class.getMethod("sort", List.class, Comparator.class);

        assertEquals(InternalUtils.asString(m), "java.util.Collections.sort(List, Comparator)");
    }

    @Test
    public void method_as_string_primitive_arg() throws Exception
    {
        Method m = Object.class.getMethod("wait", long.class);

        assertEquals(InternalUtils.asString(m), "java.lang.Object.wait(long)");
    }

    @Test
    public void method_as_string_primitive_array_arg() throws Exception
    {
        Method m = Arrays.class.getMethod("sort", int[].class);

        assertEquals(InternalUtils.asString(m), "java.util.Arrays.sort(int[])");
    }

    @Test
    public void method_as_string_array_arg() throws Exception
    {
        Method m = Arrays.class.getMethod("sort", Object[].class);

        assertEquals(InternalUtils.asString(m), "java.util.Arrays.sort(Object[])");
    }

    @Test
    public void array_size_when_null()
    {
        assertEquals(InternalUtils.size(null), 0);
    }

    @Test
    public void array_size_when_non_null()
    {
        Object[] array =
        { 1, 2, 3 };

        assertEquals(InternalUtils.size(array), 3);
    }

    @Test(dataProvider = "memberPrefixData")
    public void strip_member_prefix(String input, String expected)
    {
        assertEquals(InternalUtils.stripMemberPrefix(input), expected);
    }

    @DataProvider(name = "memberPrefixData")
    public Object[][] memberPrefixData()
    {
        return new Object[][]
        {
        { "simple", "simple" },
        { "_name", "name" },
        { "$name", "name" },
        { "$_$__$__$_$___$_$_$_$$name$", "name$" } };
    }

    @Test
    public void enumeration_to_list()
    {
        List<String> input = Arrays.asList("wilma", "fred", "barney");
        Enumeration e = Collections.enumeration(input);

        List<String> output = toList(e);

        assertEquals(output, Arrays.asList("barney", "fred", "wilma"));
    }

    @Test
    public void join_empty_list()
    {
        List<String> empty = CollectionFactory.newList();

        assertEquals(InternalUtils.join(empty), "");
    }

    @Test
    public void join_single()
    {
        List<String> single = Arrays.asList("barney");

        assertEquals(InternalUtils.join(single), "barney");
    }

    @Test
    public void join_multiple()
    {
        List<String> many = Arrays.asList("fred", "barney", "wilma");
        assertEquals(InternalUtils.join(many), "fred, barney, wilma");
    }

    @Test
    public void join_sorted()
    {
        List<String> unsorted = Arrays.asList("betty", "fred", "barney", "wilma");
        List<String> copy = CollectionFactory.newList(unsorted);

        assertEquals(InternalUtils.joinSorted(copy), "barney, betty, fred, wilma");

        // Make sure that joinSorted() doesn't change the input list

        assertEquals(copy, unsorted);
    }

    @Test(dataProvider = "capitalize_inputs")
    public void capitalize(String input, String expected)
    {
        assertEquals(InternalUtils.capitalize(input), expected);
    }

    @DataProvider(name = "capitalize_inputs")
    public Object[][] capitalize_inputs()
    {
        return new Object[][]
        {
        { "hello", "Hello" },
        { "Goodbye", "Goodbye" },
        { "", "" },
        { "a", "A" },
        { "A", "A" } };
    }

    @Test
    public void location_of_not_found()
    {
        assertNull(InternalUtils.locationOf(null));
        assertNull(InternalUtils.locationOf("La! La!"));
    }

    @Test
    public void location_of_location()
    {
        Location l = newLocation();

        replay();

        assertSame(l, InternalUtils.locationOf(l));

        verify();
    }

    @Test
    public void location_of_locatable()
    {
        Location l = newLocation();
        Locatable locatable = newMock(Locatable.class);

        expect(locatable.getLocation()).andReturn(l);

        replay();

        assertSame(l, InternalUtils.locationOf(locatable));

        verify();
    }

    @Test
    public void sorted_keys_from_null_map()
    {
        List<String> list = InternalUtils.sortedKeys(null);

        assertTrue(list.isEmpty());
    }

    @Test
    public void sorted_keys_from_map()
    {
        Map<String, String> map = newMap();

        map.put("fred", "flintstone");
        map.put("barney", "rubble");

        assertEquals(InternalUtils.sortedKeys(map), Arrays.asList("barney", "fred"));
    }

    @Test
    public void get_from_null_map()
    {
        assertNull(InternalUtils.get(null, null));
    }

    @Test
    public void get_from_map()
    {
        Map<String, String> map = newMap();

        map.put("fred", "flintstone");

        assertEquals("flintstone", InternalUtils.get(map, "fred"));
    }

    @Test
    public void reverse_iterator()
    {
        List<String> list = Arrays.asList("a", "b", "c");

        Iterator<String> i = InternalUtils.reverseIterator(list);

        assertTrue(i.hasNext());
        assertEquals(i.next(), "c");

        assertTrue(i.hasNext());
        assertEquals(i.next(), "b");

        assertTrue(i.hasNext());
        assertEquals(i.next(), "a");

        assertFalse(i.hasNext());
    }

    @Test
    public void reverse_iterator_does_not_support_remove()
    {
        List<String> list = Arrays.asList("a", "b", "c");

        Iterator<String> i = InternalUtils.reverseIterator(list);

        try
        {
            i.remove();
            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {

        }
    }

}
