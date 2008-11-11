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

package org.apache.tapestry5.ioc.util;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.util.*;

public class StrategyRegistryTest extends IOCTestCase
{
    @Test
    public void adapter_not_found()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);

        try
        {
            r.get(Set.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No adapter from type java.util.Set to type java.lang.Runnable is available (registered types are java.util.List, java.util.Map).");
        }

        verify();
    }

    @Test
    public void get_types()
    {

        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();
        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);


        Collection<Class> types = r.getTypes();

        assertEquals(types.size(), 2);
        assertTrue(types.contains(List.class));
        assertTrue(types.contains(Map.class));

        verify();
    }

    @Test
    public void adapter_not_found_when_non_error()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);

        Runnable actual = r.get(ArrayList.class);

        assertSame(actual, r1);

        // The cache is almost impossible to "test", but we can at least collect some
        // code coverage over those lines.

        Runnable actual2 = r.get(ArrayList.class);
        assertSame(actual2, r1);

        r.clearCache();

        Runnable actual3 = r.get(ArrayList.class);
        assertSame(actual3, r1);

        verify();
    }

    @Test
    public void registration_map_is_copied_by_constructor()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);

        registrations.clear();

        Runnable actual = r.get(ArrayList.class);

        assertSame(actual, r1);
    }

    @Test
    public void adapter_found_by_instance()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);

        assertSame(r.getByInstance(registrations), r2);

        verify();
    }

    @Test
    public void null_instance_matches_class_void()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();
        Runnable r3 = mockRunnable();

        replay();

        Map<Class, Runnable> registrations = newMap();

        registrations.put(List.class, r1);
        registrations.put(Map.class, r2);
        registrations.put(void.class, r3);

        StrategyRegistry<Runnable> r = StrategyRegistry.newInstance(Runnable.class, registrations);

        assertSame(r.getByInstance(null), r3);

        verify();
    }
}
