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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Location;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.BindingSource;
import org.testng.annotations.Test;

import java.util.Map;

public class BindingSourceImplTest extends InternalBaseTestCase
{
    @Test
    public void expression_has_no_prefix()
    {
        BindingFactory factory = mockBindingFactory();
        Binding binding = mockBinding();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        Location l = mockLocation();

        String defaultPrefix = "def";
        String description = "descrip";
        String expression = "full expression";

        train_newBinding(factory, description, container, component, expression, l, binding);

        replay();

        Map<String, BindingFactory> map = newMap();

        map.put(defaultPrefix, factory);

        BindingSource source = new BindingSourceImpl(map);

        Binding actual = source.newBinding(
                description,
                container,
                component,
                defaultPrefix,
                expression,
                l);

        assertSame(actual, binding);

        verify();
    }

    @Test
    public void expression_prefix_not_in_configuration()
    {
        BindingFactory factory = mockBindingFactory();
        Binding binding = mockBinding();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        Location l = mockLocation();

        String defaultPrefix = "def";
        String description = "descrip";
        String expression = "javascript:not-a-known-prefix";

        train_newBinding(factory, description, container, component, expression, l, binding);

        replay();

        Map<String, BindingFactory> map = newMap();

        map.put(defaultPrefix, factory);

        BindingSource source = new BindingSourceImpl(map);

        Binding actual = source.newBinding(
                description,
                container,
                component,
                defaultPrefix,
                expression,
                l);

        assertSame(actual, binding);

        verify();
    }

    @Test
    public void known_prefix()
    {
        BindingFactory factory = mockBindingFactory();
        Binding binding = mockBinding();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        Location l = mockLocation();

        String defaultPrefix = "literal";
        String description = "descrip";

        // The "prop:" prefix is stripped off ...
        train_newBinding(factory, description, container, component, "myproperty", l, binding);

        replay();

        Map<String, BindingFactory> map = newMap();

        map.put("prop", factory);

        BindingSource source = new BindingSourceImpl(map);

        Binding actual = source.newBinding(
                description,
                container,
                component,
                defaultPrefix,
                "prop:myproperty",
                l);

        assertSame(actual, binding);

        verify();
    }

    @Test
    public void factory_throws_exception()
    {
        BindingFactory factory = mockBindingFactory();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        Location l = mockLocation();
        Throwable t = new RuntimeException("Simulated failure.");

        String defaultPrefix = "def";
        String description = "descrip";
        String expression = "full expression";

        factory.newBinding(description, container, component, expression, l);
        setThrowable(t);

        replay();

        Map<String, BindingFactory> map = newMap();

        map.put(defaultPrefix, factory);

        BindingSource source = new BindingSourceImpl(map);

        try
        {
            source.newBinding(description, container, component, defaultPrefix, expression, l);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertTrue(ex.getMessage().contains(
                    "Could not convert 'full expression' into a component parameter binding"));
            assertTrue(ex.getMessage().contains(t.getMessage()));
            assertSame(ex.getLocation(), l);
            assertSame(ex.getCause(), t);
        }

        verify();
    }
}
