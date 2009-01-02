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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ModuleImplTest extends IOCInternalTestCase
{
    protected final void train_newClass(InternalRegistry registry, ClassFactory factory, Class serviceInterface)
    {
        expect(registry.newClass(serviceInterface)).andReturn(factory.newClass(serviceInterface));
    }

    @Test
    public void find_service_ids_for_interface()
    {
        InternalRegistry registry = mockInternalRegistry();
        Logger logger = mockLogger();

        ModuleDef moduleDef = new DefaultModuleDefImpl(ModuleImplTestModule.class, logger, null);

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        replay();

        Collection<String> ids = module.findServiceIdsForInterface(FieService.class);

        assertEquals(ids.size(), 2);
        assertTrue(ids.contains("Fie"));
        assertTrue(ids.contains("OtherFie"));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void find_decorator_defs_for_service()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        DecoratorDef def1 = mockDecoratorDef();
        DecoratorDef def2 = mockDecoratorDef();
        Set<DecoratorDef> rawDefs = newMock(Set.class);
        Logger logger = mockLogger();

        ModuleDef moduleDef = mockModuleDef();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getDecoratorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<DecoratorDef> defs = module.findMatchingDecoratorDefs(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }


    protected void train_expandSymbols(InternalRegistry registry, String input, String expanded)
    {
        expect(registry.expandSymbols(input)).andReturn(expanded);
    }

    private Registry buildRegistry()
    {
        RegistryBuilder builder = new RegistryBuilder();
        builder.add(ModuleImplTestModule.class);

        return builder.build();
    }

    /**
     * The following tests work better as integration tests.
     */

    @Test
    public void integration_tests()
    {
        Registry registry = buildRegistry();

        UpcaseService us = registry.getService(UpcaseService.class);

        assertEquals(us.upcase("hello"), "HELLO");
        assertEquals(us.toString(), "<Proxy for Upcase(org.apache.tapestry5.ioc.internal.UpcaseService)>");

        ToStringService ts = registry.getService(ToStringService.class);

        assertEquals(ts.toString(), "<ToStringService: ToString>");

        registry.shutdown();
    }

    @Test
    public void recursive_singleton_integration_test()
    {
        Registry registry = buildRegistry();

        FoeService foe = registry.getService("RecursiveFoe", FoeService.class);

        try
        {
            foe.foe();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // The details are checked elsewhere.
        }

        registry.shutdown();
    }
}
