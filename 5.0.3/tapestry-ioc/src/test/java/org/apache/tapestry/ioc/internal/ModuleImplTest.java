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

package org.apache.tapestry.ioc.internal;

import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.isA;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.testng.annotations.Test;

public class ModuleImplTest extends IOCInternalTestCase
{
    @Test
    public void get_service_by_id_exists()
    {
        InternalRegistry registry = newInternalRegistry();
        Log log = newLog();
        ClassFactory factory = new ClassFactoryImpl();

        ModuleDef moduleDef = new DefaultModuleDefImpl(ModuleImplTestModule.class, log, null);

        Module module = new ModuleImpl(registry, moduleDef, log);

        expect(registry.logForService("Upcase")).andReturn(log);

        train_isDebugEnabled(log, true);
        log.debug("Creating service 'Upcase'.");

        train_getLifecycle(registry, "singleton", new SingletonServiceLifecycle());

        train_newClass(registry, factory, UpcaseService.class);

        registry.addRegistryShutdownListener(isA(RegistryShutdownListener.class));

        train_isDebugEnabled(log, false);

        train_findDecoratorsForService(registry);

        replay();

        UpcaseService service = module.getService("Upcase", UpcaseService.class);

        assertEquals(service.upcase("hello"), "HELLO");

        verify();
    }

    protected final void train_newClass(InternalRegistry registry, ClassFactory factory,
            Class serviceInterface)
    {
        expect(registry.newClass(serviceInterface)).andReturn(factory.newClass(serviceInterface));
    }

    @Test
    public void find_service_ids_for_interface()
    {
        InternalRegistry registry = newInternalRegistry();
        Log log = newLog();

        ModuleDef moduleDef = new DefaultModuleDefImpl(ModuleImplTestModule.class, log, null);

        Module module = new ModuleImpl(registry, moduleDef, log);

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
        InternalRegistry registry = newInternalRegistry();
        ServiceDef serviceDef = newServiceDef();
        DecoratorDef def1 = newDecoratorDef();
        DecoratorDef def2 = newDecoratorDef();
        Set<DecoratorDef> rawDefs = newMock(Set.class);
        Log log = newLog();

        ModuleDef moduleDef = newModuleDef();

        expect(moduleDef.getDecoratorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, moduleDef, log);

        Set<DecoratorDef> defs = module.findMatchingDecoratorDefs(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }

    @Test
    public void no_public_constructor_on_module_builder_class()
    {
        InternalRegistry registry = newInternalRegistry();
        Log log = newLog();
        ModuleDef def = new DefaultModuleDefImpl(PrivateConstructorModule.class, log, null);

        replay();

        Module module = new ModuleImpl(registry, def, log);

        try
        {
            module.getModuleBuilder();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Module builder class org.apache.tapestry.ioc.internal.PrivateConstructorModule "
                            + "does not contain any public constructors.");
        }

        verify();

    }

    @Test
    public void too_many_public_constructors_on_module_builder_class()
    {
        InternalRegistry registry = newInternalRegistry();
        Log log = newLog();
        ModuleDef def = new DefaultModuleDefImpl(ExtraPublicConstructorsModule.class, log, null);
        ClassFactory factory = newMock(ClassFactory.class);
        Module module = new ModuleImpl(registry, def, log);

        log.warn(contains("contains more than one public constructor"));

        train_expandSymbols(registry, "ClassFactory", "ClassFactory");

        train_getService(registry, "ClassFactory", ClassFactory.class, factory);

        replay();

        assertTrue(module.getModuleBuilder() instanceof ExtraPublicConstructorsModule);

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

    /** The following tests work better as integration tests. */

    @Test
    public void integration_tests()
    {
        Registry registry = buildRegistry();

        UpcaseService us = registry.getService(UpcaseService.class);

        assertEquals(us.upcase("hello"), "HELLO");
        assertEquals(
                us.toString(),
                "<Proxy for Upcase(org.apache.tapestry.ioc.internal.UpcaseService)>");

        ToStringService ts = registry.getService(ToStringService.class);

        assertEquals(ts.toString(), "<ToStringService: ToString>");
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
    }

}
