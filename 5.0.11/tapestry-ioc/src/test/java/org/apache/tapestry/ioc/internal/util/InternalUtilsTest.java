// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.ioc.Locatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.annotations.Inject;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.InternalUtils.toList;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

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
        Object[] array = { 1, 2, 3 };

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
        return new Object[][] { { "simple", "simple" }, { "_name", "name" }, { "$name", "name" },
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
    public void join_with_blank()
    {
        List<String> many = Arrays.asList("fred", "barney", "", "wilma");
        assertEquals(InternalUtils.join(many), "fred, barney, (blank), wilma");

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

    @Test
    public void join_sorted_with_blank()
    {
        List<String> unsorted = Arrays.asList("betty", "fred", "barney", "", "wilma");

        assertEquals(InternalUtils.joinSorted(unsorted), "(blank), barney, betty, fred, wilma");

    }

    @Test(dataProvider = "capitalize_inputs")
    public void capitalize(String input, String expected)
    {
        assertEquals(InternalUtils.capitalize(input), expected);
    }

    @DataProvider(name = "capitalize_inputs")
    public Object[][] capitalize_inputs()
    {
        return new Object[][] { { "hello", "Hello" }, { "Goodbye", "Goodbye" }, { "", "" }, { "a", "A" },
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
        Location l = mockLocation();

        replay();

        assertSame(l, InternalUtils.locationOf(l));

        verify();
    }

    @Test
    public void location_of_locatable()
    {
        Location l = mockLocation();
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

    @Test
    public void last_term()
    {
        String input = "SimpleInput";

        assertSame(InternalUtils.lastTerm(input), input);

        assertEquals(InternalUtils.lastTerm("fie.fie.foe.fum"), "fum");
    }

    @Test
    public void add_to_list_map()
    {
        Map<String, List<Integer>> map = CollectionFactory.newMap();

        InternalUtils.addToMapList(map, "fred", 1);

        assertEquals(map.get("fred"), Arrays.asList(1));

        InternalUtils.addToMapList(map, "fred", 2);

        assertEquals(map.get("fred"), Arrays.asList(1, 2));
    }

    // Test the check for runtime annotations. This is all well and good, we actually don't have a proper test
    // that this code is used (ideally we should have tests for @Marker on a module, on a service impl, and passed
    // to ServiceBindingOptions.withMarker(), to prove that those are wired for checks.

    @Test
    public void validate_marker_annotation()
    {
        InternalUtils.validateMarkerAnnotation(Inject.class);


        try
        {
            InternalUtils.validateMarkerAnnotations(new Class[] { Inject.class, NotRetainedRuntime.class });
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Marker annotation class org.apache.tapestry.ioc.internal.util.NotRetainedRuntime is not valid because it is not visible at runtime. Add a @RetentionPolicy(RUNTIME) to the class.");
        }
    }

    @Test
    public void join_sorted_null()
    {
        assertEquals(InternalUtils.joinSorted(null), "(none)");
    }

    @Test
    public void join_sorted_empty()
    {
        assertEquals(InternalUtils.joinSorted(Collections.emptyList()), "(none)");
    }

    @Test
    public void close_null_is_noop()
    {
        InternalUtils.close(null);
    }

    @Test
    public void close_success() throws Exception
    {
        Closeable c = newMock(Closeable.class);

        c.close();

        replay();

        InternalUtils.close(c);

        verify();
    }

    @Test
    public void close_ignores_exceptions() throws Exception
    {
        Closeable c = newMock(Closeable.class);

        c.close();
        setThrowable(new IOException());

        replay();

        InternalUtils.close(c);

        verify();
    }

}
