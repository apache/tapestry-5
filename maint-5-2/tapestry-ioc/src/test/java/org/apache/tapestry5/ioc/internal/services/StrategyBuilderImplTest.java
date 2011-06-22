// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.StrategyBuilder;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StrategyBuilderImplTest extends IOCInternalTestCase
{
    private static class KindOfImpl implements KindOf
    {
        private final String value;

        public KindOfImpl(final String value)
        {
            this.value = value;
        }

        public String kindOf(Object value)
        {
            return this.value;
        }
    }

    @Test
    public void standard()
    {
        StrategyRegistry<KindOf> registry = buildStrategyRegistry();

        StrategyBuilder builder = getService(StrategyBuilder.class);

        KindOf service = builder.build(registry);

        assertEquals(service.kindOf(Collections.EMPTY_MAP), "MAP");
        assertEquals(service.kindOf(Collections.EMPTY_LIST), "LIST");

        assertEquals(service.toString(), "<Strategy for org.apache.tapestry5.ioc.internal.services.KindOf>");

        try
        {
            service.kindOf(null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "No adapter from type void to type org.apache.tapestry5.ioc.internal.services.KindOf is available (registered types are java.util.List, java.util.Map).");
        }
    }

    @Test
    public void using_registration_map()
    {
        Map<Class, KindOf> registrations = CollectionFactory.newMap();

        registrations.put(Map.class, new KindOfImpl("MAP"));
        registrations.put(List.class, new KindOfImpl("LIST"));
        StrategyBuilder builder = getService(StrategyBuilder.class);

        KindOf service = builder.build(KindOf.class, registrations);

        assertEquals(service.kindOf(Collections.EMPTY_MAP), "MAP");
        assertEquals(service.kindOf(Collections.EMPTY_LIST), "LIST");
    }

    private StrategyRegistry<KindOf> buildStrategyRegistry()
    {
        Map<Class, KindOf> registrations = CollectionFactory.newMap();

        registrations.put(Map.class, new KindOfImpl("MAP"));
        registrations.put(List.class, new KindOfImpl("LIST"));

        return StrategyRegistry.newInstance(KindOf.class, registrations);
    }
}
