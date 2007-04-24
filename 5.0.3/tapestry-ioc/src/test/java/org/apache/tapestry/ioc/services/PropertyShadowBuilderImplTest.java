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

package org.apache.tapestry.ioc.services;

import java.util.Map;

import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertyShadowBuilderImplTest extends IOCInternalTestCase
{
    private PropertyShadowBuilder _builder;

    private final String CLASS_NAME = getClass().getName();

    @BeforeClass
    public void setupBuilder()
    {
        Registry registry = buildRegistry();

        _builder = registry.getService("PropertyShadowBuilder", PropertyShadowBuilder.class);
    }

    public class FooHolder
    {
        private Foo _foo;

        private int _count = 0;

        public Foo getFoo()
        {
            _count++;

            return _foo;
        }

        public int getCount()
        {
            return _count;
        }

        public void setFoo(Foo foo)
        {
            _foo = foo;
        }

        @Override
        public String toString()
        {
            return "[FooHolder]";
        }

        public void setWriteOnly(Foo foo)
        {

        }
    }

    public interface Foo
    {
        void foo();
    }

    @Test
    public void basic_delegation()
    {
        Foo foo = newMock(Foo.class);
        FooHolder holder = new FooHolder();

        holder.setFoo(foo);

        Foo shadow = _builder.build(holder, "foo", Foo.class);

        for (int i = 0; i < 3; i++)
        {
            foo.foo();

            replay();

            shadow.foo();

            verify();

            assertEquals(holder.getCount(), i + 1);
        }

        assertEquals(shadow.toString(), "<Shadow: property foo of [FooHolder]>");
    }

    @Test
    public void property_does_not_exist()
    {
        FooHolder holder = new FooHolder();

        try
        {
            _builder.build(holder, "bar", Foo.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Class " + CLASS_NAME
                    + "$FooHolder does not contain a property named 'bar'.");
        }
    }

    @Test
    public void property_type_mismatch()
    {
        FooHolder holder = new FooHolder();

        try
        {
            _builder.build(holder, "count", Map.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Property 'count' of class " + CLASS_NAME
                    + "$FooHolder is of type int, which is not assignable to type java.util.Map.");
        }
    }

    @Test
    public void property_write_only()
    {
        FooHolder holder = new FooHolder();

        try
        {
            _builder.build(holder, "writeOnly", Foo.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class "
                            + CLASS_NAME
                            + "$FooHolder does not provide an accessor ('getter') method for property 'writeOnly'.");
        }
    }
}
