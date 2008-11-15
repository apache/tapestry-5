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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class InheritanceSearchTest extends TestBase
{

    @Test
    public void remove_always_fails()
    {
        try
        {
            new InheritanceSearch(Object.class).remove();
            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {

        }
    }

    @Test
    public void next_when_no_more()
    {
        InheritanceSearch s = new InheritanceSearch(Object.class);

        assertSame(s.next(), Object.class);
        assertFalse(s.hasNext());

        try
        {
            s.next();
            unreachable();
        }
        catch (IllegalStateException ex)
        {

        }
    }

    @Test
    public void inheritance_of_object()
    {
        check(Object.class, Object.class);
    }

    @Test
    public void inheritance_of_string()
    {
        check(
                String.class,
                String.class,
                Serializable.class,
                Comparable.class,
                CharSequence.class,
                Object.class);
    }

    @Test
    public void inheritance_of_an_interface()
    {
        check(Comparable.class, Comparable.class, Object.class);
    }

    @Test
    public void inheritance_search_order_for_interfaces()
    {
        check(FooBar.class, FooBar.class, Foo.class, Bar.class, Object.class);
    }

    @Test
    public void inheritance_search_order_for_classes()
    {
        check(
                FooBarImpl.class,
                FooBarImpl.class,
                FooImpl.class,
                BarImpl.class,
                Bar.class,
                FooBar.class,
                Foo.class,
                Object.class);

    }

    @Test
    public void inheritance_of_primitive()
    {
        check(
                long.class,
                long.class,
                Long.class,
                Number.class,
                Comparable.class,
                Serializable.class,
                Object.class);
    }

    @Test
    public void inheritance_of_void()
    {
        check(void.class, void.class, Object.class);
    }

    @Test
    public void inheritance_of_primitive_array()
    {
        check(long[].class, long[].class, Cloneable.class, Serializable.class, Object.class);
    }

    @Test
    public void inheritance_of_a_2d_primitive_array()
    {
        check(int[][].class, int[][].class, Cloneable.class, Serializable.class, Object.class);
    }

    @Test
    public void inheritance_of_an_object_array()
    {
        check(
                String[].class,
                String[].class,
                Object[].class,
                Cloneable.class,
                Serializable.class,
                Object.class);
    }

    @Test
    public void inheritance_of_a_2d_object_array()
    {
        check(
                String[][].class,
                String[][].class,
                Object[].class,
                Cloneable.class,
                Serializable.class,
                Object.class);
    }

    private void check(Class searchClass, Class... expected)
    {
        List<Class> list = CollectionFactory.newList();

        // This for loop is how the class is generally used:

        for (Class c : new InheritanceSearch(searchClass))
            list.add(c);

        assertEquals(list.toArray(), expected);
    }
}
