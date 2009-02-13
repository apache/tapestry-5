// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef2;
import org.apache.tapestry5.ioc.internal.QuietOperationTracker;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry5.ioc.internal.util.InternalUtils.toList;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.easymock.IAnswer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class InternalUtilsTest extends IOCTestCase
{
    private final OperationTracker tracker = new QuietOperationTracker();

    private static class PrivateInnerClass
    {
        public PrivateInnerClass()
        {
        }
    }

    public static class PublicInnerClass
    {
        protected PublicInnerClass()
        {
        }
    }

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
        Object[] array = null;

        assertEquals(InternalUtils.size(array), 0);
    }

    @Test
    public void array_size_when_non_null()
    {
        Object[] array = { 1, 2, 3 };

        assertEquals(InternalUtils.size(array), 3);
    }

    @Test(dataProvider = "memberNameData")
    public void strip_member_name(String input, String expected)
    {
        assertEquals(InternalUtils.stripMemberName(input), expected);
    }

    @DataProvider
    public Object[][] memberNameData()
    {
        return new Object[][] {
                { "simple", "simple" },
                { "_name", "name" },
                { "$name", "name" },
                { "ruby_style", "ruby_style" },
                { "__$ruby_style_", "ruby_style" },
                { "$_$__$__$_$___$_$_$_$$name$", "name" },
                { "foo_", "foo" },
                { "_foo_", "foo" }
        };
    }

    @Test
    public void strip_illegal_member_name()
    {
        try
        {
            InternalUtils.stripMemberName("!foo");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Input '!foo' is not a valid Java identifier.");
        }
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

    // Test the check for runtime annotation. This is all well and good, we actually don't have a proper test
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
                         "Marker annotation class org.apache.tapestry5.ioc.internal.util.NotRetainedRuntime is not valid because it is not visible at runtime. Add a @RetentionPolicy(RUNTIME) to the class.");
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

    @Test
    public void constructor_with_inject_annotation()
    {
        Constructor c = InternalUtils.findAutobuildConstructor(InjectoBean.class);

        assertEquals(c.getParameterTypes().length, 1);
        assertEquals(c.getParameterTypes()[0], String.class);
    }

    @Test
    public void validate_constructor_class_not_public()
    {
        Class clazz = PrivateInnerClass.class;
        Constructor cons = clazz.getConstructors()[0];

        try
        {
            InternalUtils.validateConstructorForAutobuild(cons);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Class org.apache.tapestry5.ioc.internal.util.InternalUtilsTest$PrivateInnerClass is not a public class and may not be autobuilt.");
        }
    }

    @Test
    public void validate_constructor_check_for_public()
    {
        Class clazz = PublicInnerClass.class;
        Constructor cons = clazz.getDeclaredConstructors()[0];

        try
        {
            InternalUtils.validateConstructorForAutobuild(cons);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex,
                                  "Constructor protected org.apache.tapestry5.ioc.internal.util.InternalUtilsTest$PublicInnerClass() is not public and may not be used for autobuilding an instance of the class.");
        }
    }

    @Test
    public void inject_service_annotation_on_field()
    {
        ObjectLocator ol = mockObjectLocator();
        FieldInjectionViaInjectService target = new FieldInjectionViaInjectService();
        Runnable fred = mockRunnable();

        train_getService(ol, "FredService", Runnable.class, fred);

        replay();

        InternalUtils.injectIntoFields(target, ol, null, tracker);

        assertSame(target.getFred(), fred);

        verify();
    }

    @Test
    public void inject_annotation_on_field()
    {
        ObjectLocator ol = mockObjectLocator();
        FieldInjectionViaInject target = new FieldInjectionViaInject();
        final SymbolSource ss = mockSymbolSource();

        IAnswer answer = new IAnswer()
        {
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();

                AnnotationProvider ap = (AnnotationProvider) args[1];

                // Verify that annotations on the field are accessible.

                assertNotNull(ap.getAnnotation(Builtin.class));

                return ss;
            }
        };

        expect(ol.getObject(eq(SymbolSource.class), isA(AnnotationProvider.class))).andAnswer(answer);

        replay();

        InternalUtils.injectIntoFields(target, ol, null, tracker);

        assertSame(target.getSymbolSource(), ss);

        verify();
    }

    @Test
    public void exception_injecting_into_field()
    {
        ObjectLocator ol = mockObjectLocator();
        FieldInjectionViaInjectService target = new FieldInjectionViaInjectService();

        // It's very hard to come up with a value that causes an error when assigned. We have to break
        // a lot of rules.

        ol.getService("FredService", Runnable.class);
        EasyMock.expectLastCall().andReturn("NotTheRightType");

        replay();

        try
        {
            InternalUtils.injectIntoFields(target, ol, null, tracker);

            unreachable();
        }
        catch (Exception ex)
        {
            assertMessageContains(ex,
                                  "Unable to set field 'fred' of <FieldInjectionViaInjectService> to NotTheRightType");
        }


        verify();
    }

    @Test
    public void keys_on_null_is_empty()
    {
        assertTrue(InternalUtils.keys(null).isEmpty());
    }

    @Test
    public void keys_on_actual_map()
    {
        Map<String, Integer> map = CollectionFactory.newMap();

        map.put("frobozz", 41);
        map.put("gnip", 97);

        assertSame(InternalUtils.keys(map), map.keySet());
    }

    @Test
    public void collection_size()
    {
        Collection c = null;

        assertEquals(InternalUtils.size(c), 0);

        c = Arrays.asList("moe", "larry", "curly");

        assertEquals(InternalUtils.size(c), 3);
    }

    @Test
    public void servicedef_to_servicedef2()
    {
        final ObjectCreator oc = mockObjectCreator();
        final String serviceId = "RocketLauncher";
        final Set<Class> markers = Collections.emptySet();
        final Class serviceInterface = Runnable.class;

        ServiceDef sd = new ServiceDef()
        {
            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return oc;
            }

            public String getServiceId()
            {
                return serviceId;
            }

            public Set<Class> getMarkers()
            {
                return markers;
            }

            public Class getServiceInterface()
            {
                return serviceInterface;
            }

            public String getServiceScope()
            {
                return ScopeConstants.PERTHREAD;
            }

            public boolean isEagerLoad()
            {
                return true;
            }
        };

        replay();

        ServiceDef2 sd2 = InternalUtils.toServiceDef2(sd);

        assertSame(sd2.createServiceCreator(null), oc);
        assertSame(sd2.getServiceId(), serviceId);
        assertSame(sd2.getMarkers(), markers);
        assertSame(sd2.getServiceInterface(), serviceInterface);
        assertSame(sd2.getServiceScope(), ScopeConstants.PERTHREAD);
        assertTrue(sd2.isEagerLoad());
        assertFalse(sd2.isPreventDecoration());

        verify();
    }
}
