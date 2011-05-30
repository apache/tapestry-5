// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
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
        DecoratorDef2 def1 =newMock(DecoratorDef2.class);
        DecoratorDef def2 = newMock(DecoratorDef2.class);
        Set<DecoratorDef> rawDefs = newMock(Set.class);
        Logger logger = mockLogger();

        ModuleDef moduleDef = mockModuleDef();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getDecoratorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        expect(def1.getServiceInterface()).andReturn(ToStringService.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<DecoratorDef> defs = module.findMatchingDecoratorDefs(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void find_decorator_defs_for_service_markers_do_not_match()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        DecoratorDef2 def1 =newMock(DecoratorDef2.class);
        DecoratorDef def2 = newMock(DecoratorDef2.class);
        Set<DecoratorDef> rawDefs = newMock(Set.class);
        Set<Class> def1Markers = CollectionFactory.newSet((Class)BlueMarker.class);
        Set<Class> serviceDefMarkers =CollectionFactory.newSet((Class)RedMarker.class);
        Set<Class> allMarkers = CollectionFactory.newSet((Class)BlueMarker.class, (Class)BlueMarker.class);
        Logger logger = mockLogger();

        ModuleDef moduleDef = mockModuleDef();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getDecoratorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        expect(def1.getServiceInterface()).andReturn(Object.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        expect(def1.getMarkers()).andReturn(def1Markers);
        expect(registry.getMarkerAnnotations()).andReturn(allMarkers);
        expect(serviceDef.getMarkers()).andReturn(serviceDefMarkers);
        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<DecoratorDef> defs = module.findMatchingDecoratorDefs(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void find_advisor_defs_for_service()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        AdvisorDef2 def1 = mockAdvisorDef2();
        AdvisorDef2 def2 = mockAdvisorDef2();
        Set<AdvisorDef> rawDefs = newMock(Set.class);
        Logger logger = mockLogger();

        ModuleDef2 moduleDef = mockModuleDef2();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getAdvisorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.<AdvisorDef>asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        expect(def1.getServiceInterface()).andReturn(ToStringService.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<AdvisorDef> defs = module.findMatchingServiceAdvisors(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void find_advisor_defs_for_service_markers_do_not_match()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        AdvisorDef2 def1 = mockAdvisorDef2();
        AdvisorDef2 def2 = mockAdvisorDef2();
        Set<AdvisorDef> rawDefs = newMock(Set.class);
        Set<Class> def1Markers = CollectionFactory.newSet((Class)BlueMarker.class);
        Set<Class> serviceDefMarkers =CollectionFactory.newSet((Class)RedMarker.class);
        Set<Class> allMarkers = CollectionFactory.newSet((Class)BlueMarker.class, (Class)BlueMarker.class);
        Logger logger = mockLogger();

        ModuleDef2 moduleDef = mockModuleDef2();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getAdvisorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.<AdvisorDef>asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        expect(def1.getServiceInterface()).andReturn(Object.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        expect(def1.getMarkers()).andReturn(def1Markers);
        expect(registry.getMarkerAnnotations()).andReturn(allMarkers);
        expect(serviceDef.getMarkers()).andReturn(serviceDefMarkers);

        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<AdvisorDef> defs = module.findMatchingServiceAdvisors(serviceDef);

        assertEquals(defs.size(), 1);
        assertTrue(defs.contains(def2));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void find_advisor_defs_for_service_markers_do_match()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        AdvisorDef2 def1 = mockAdvisorDef2();
        AdvisorDef2 def2 = mockAdvisorDef2();
        Set<AdvisorDef> rawDefs = newMock(Set.class);
        Set<Class> def1Markers = CollectionFactory.newSet((Class)BlueMarker.class);
        Set<Class> serviceDefMarkers =CollectionFactory.newSet((Class)BlueMarker.class);
        Set<Class> allMarkers = CollectionFactory.newSet((Class)BlueMarker.class);
        Logger logger = mockLogger();

        ModuleDef2 moduleDef = mockModuleDef2();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getAdvisorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.<AdvisorDef>asList(def1, def2).iterator());

        train_matches(def1, serviceDef, false);
        expect(def1.getServiceInterface()).andReturn(Object.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        expect(def1.getMarkers()).andReturn(def1Markers);
        expect(registry.getMarkerAnnotations()).andReturn(allMarkers);
        expect(serviceDef.getMarkers()).andReturn(serviceDefMarkers);

        train_matches(def2, serviceDef, true);

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<AdvisorDef> defs = module.findMatchingServiceAdvisors(serviceDef);

        assertEquals(defs.size(), 2);
        assertTrue(defs.contains(def1));
        assertTrue(defs.contains(def2));

        verify();
    }


     @SuppressWarnings("unchecked")
    @Test
    public void no_advisor_def_when_service_interface_is_default_and_no_markers()
    {
        InternalRegistry registry = mockInternalRegistry();
        ServiceDef serviceDef = mockServiceDef();
        AdvisorDef2 def = mockAdvisorDef2();
        Set<AdvisorDef> rawDefs = newMock(Set.class);
        Logger logger = mockLogger();

        ModuleDef2 moduleDef = mockModuleDef2();

        Set<String> serviceIds = Collections.emptySet();
        expect(moduleDef.getServiceIds()).andReturn(serviceIds);

        expect(moduleDef.getAdvisorDefs()).andReturn(rawDefs);

        expect(rawDefs.iterator()).andReturn(Arrays.<AdvisorDef>asList(def).iterator());

        train_matches(def, serviceDef, false);
        expect(def.getServiceInterface()).andReturn(Object.class);
        expect(serviceDef.getServiceInterface()).andReturn(Runnable.class);
        expect(def.getMarkers()).andReturn(CollectionFactory.<Class>newSet());
        expect(registry.getMarkerAnnotations()).andReturn(CollectionFactory.<Class>newSet());

        replay();

        Module module = new ModuleImpl(registry, null, moduleDef, null, logger);

        Set<AdvisorDef> defs = module.findMatchingServiceAdvisors(serviceDef);

        assertEquals(defs.size(), 0);

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
