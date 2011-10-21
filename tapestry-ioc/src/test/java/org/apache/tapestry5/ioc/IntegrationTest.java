// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.internal.*;
import org.apache.tapestry5.ioc.internal.services.StartupModule2;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.NonmatchingMappedConfigurationOverrideModule;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * A few tests that are easiest (or even just possible) by building a Registry and trying out a few
 * things.
 */
public class IntegrationTest extends IOCInternalTestCase
{
    public static int countingGreeterInstantiationCount;

    private Registry buildRegistry()
    {
        return buildRegistry(FredModule.class, BarneyModule.class);
    }

    @Test
    public void duplicate_service_names_are_failure()
    {
        try
        {
            buildRegistry(FredModule.class, DuplicateFredModule.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().startsWith("Service id 'Fred' has already been defined by"));

            // Can't check the entire message, because we can't guarantee what order the modules
            // will be processed in.
        }
    }

    @Test
    public void static_builder_method_does_not_instantiate_builder()
    {
        StaticModule.setInstantiated(false);
        StaticModule.setFredRan(false);

        Registry r = buildRegistry(StaticModule.class);

        Runnable fred = r.getService("Fred", Runnable.class);

        fred.run();

        assertFalse(StaticModule.isInstantiated());
        assertTrue(StaticModule.getFredRan());

        r.shutdown();
    }

    @Test
    public void static_decorator_method_does_not_instantiate_builder()
    {
        StaticModule.setInstantiated(false);
        StaticModule.setDecoratorRan(false);

        Registry r = buildRegistry(StaticModule.class);

        Runnable fred = r.getService("Barney", Runnable.class);

        fred.run();

        assertFalse(StaticModule.isInstantiated());
        assertTrue(StaticModule.getDecoratorRan());

        r.shutdown();
    }

    @Test
    public void static_contributor_method_does_not_instantiate_builder()
    {
        StaticModule.setInstantiated(false);

        Registry r = buildRegistry(StaticModule.class);

        NameListHolder holder = r.getService("Names", NameListHolder.class);

        List<String> names = holder.getNames();

        assertEquals(names, Arrays.asList("Fred"));

        assertFalse(StaticModule.isInstantiated());

        r.shutdown();
    }

    @Test
    public void shutdown_deactivates_proxies()
    {
        Registry r = buildRegistry();

        Runnable service = r.getService("Fred", Runnable.class);

        service.run();

        r.shutdown();

        try
        {
            service.run();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Proxy for service Fred is no longer active because the IOC Registry has been shut down.");
        }

        // Show that toString() still works, even for a shutdown proxy.

        assertEquals(service.toString(), "<Proxy for Fred(java.lang.Runnable)>");
    }

    /**
     * Along the way, we also test a few other things, such as decorator matching and automatic
     * dependency resolution.
     */
    @Test
    public void public_service_decorator_order()
    {
        Registry r = buildRegistry();

        Runnable service = r.getService("Fred", Runnable.class);

        // Force creation

        service.run();

        List<String> names = r.getService(DecoratorList.class).getNames();

        // Note that the order of invocation appears backwards, since we build back-to-front

        assertEquals(names, Arrays.asList("gamma", "beta", "alpha"));
    }

    @Test
    public void public_service_unordered_configuration()
    {
        Registry r = buildRegistry();

        NameListHolder service = r.getService("UnorderedNames", NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("Beta", "Gamma", "UnorderedNames"));
    }

    /**
     * We don't have to do as many public/private etc. tests for the other types of configuration,
     * because the code
     * paths are so similar.
     */

    @Test
    public void service_ordered_configuration()
    {
        Registry r = buildRegistry();

        NameListHolder service = r.getService("OrderedNames", NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("BARNEY", "FRED"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void service_mapped_configuration()
    {
        Registry r = buildRegistry();

        Sizer sizer = r.getService("Sizer", Sizer.class);

        assertEquals(sizer.size(null), 0);

        // Have to be exact on type here.

        List list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(sizer.size(list), 3);

        Map map = new HashMap();
        map.put("fred", "flinstone");
        map.put("barney", "rubble");

        assertEquals(sizer.size(map), 2);

        // Random objects are size 1

        assertEquals(sizer.size(this), 1);

        r.shutdown();
    }

    @Test
    public void unknown_scope()
    {
        Registry r = buildRegistry(UnknownScopeModule.class);

        try
        {
            Runnable runnable = r.getService("UnknownScope", Runnable.class);

            runnable.run();

            unreachable();
        } catch (Exception ex)
        {
            assertMessageContains(ex, "Error building service proxy for service 'UnknownScope'",
                    "Unknown service scope 'magic'");
        }

        r.shutdown();
    }

    @Test
    public void scope_mismatch()
    {
        Registry r = buildRegistry(ScopeMismatchModule.class);

        try
        {
            r.getService(StringBuilder.class);
            unreachable();
        } catch (Exception ex)
        {
            assertMessageContains(ex,
                    "Error building service proxy for service 'ScopeRequiresAProxyAndNoInterfaceIsProvided'",
                    "Service scope 'perthread' requires a proxy");
        }

        r.shutdown();

    }

    @Test
    public void simple_perthread() throws Exception
    {
        final Registry r = buildRegistry(PerThreadModule.class);

        final StringHolder holder = r.getService(StringHolder.class);

        // Something about some of the other tests causes this one to fail
        // unless we start with cleanupThread(), there must be a loose ThreadLocal
        // hanging around causing problems.

        r.cleanupThread();

        holder.setValue("fred");
        assertEquals(holder.getValue(), "fred", holder.toString());

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Assert.assertNull(holder.getValue());

                holder.setValue("barney");
                assertEquals(holder.getValue(), "barney");

                r.cleanupThread();
            }
        };

        Thread t = new Thread(runnable);

        t.start();
        t.join();

        assertEquals(holder.getValue(), "fred");

        r.cleanupThread();

        r.shutdown();
    }

    /**
     * This test fails at times and I'm not sure why. It's some kind of interaction with other tests
     * but hard to figure
     * out. Damn ThreadLocals!
     */
    @Test
    public void registry_thread_cleanup()
    {
        Registry r = buildRegistry(PerThreadModule.class);

        r.cleanupThread();

        StringHolder holder = r.getService(StringHolder.class);

        assertNull(holder.getValue());

        holder.setValue("fred");
        assertEquals(holder.getValue(), "fred");

        r.cleanupThread();

        assertNull(holder.getValue());
    }

    @Test
    public void recursive_module_construction_is_caught()
    {
        Registry r = buildRegistry(RecursiveConstructorModule.class);

        try
        {
            Runnable runnable = r.getService("Runnable", Runnable.class);

            // We can get the proxy, but invoking a method causes
            // the module to be instantiated ... but that also invokes a method on
            // the proxy.

            runnable.run();

            unreachable();
        } catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().contains("has failed due to recursion"));
        }

        r.shutdown();
    }

    @Test
    public void eager_service_loading()
    {
        Registry r = buildRegistry(EagerLoadModule.class);

        assertFalse(EagerLoadModule._eagerLoadDidHappen, "EagerLoadModule is not in correct initial state.");

        r.performRegistryStartup();

        assertTrue(EagerLoadModule._eagerLoadDidHappen);

        r.shutdown();
    }

    @Test
    public void access_to_services_ignores_case()
    {
        Registry r = buildRegistry(FredModule.class);

        Runnable fred = r.getService("Fred", Runnable.class);

        assertSame(r.getService("FRED", Runnable.class), fred);

        r.shutdown();
    }

    @Test
    public void simple_autobuild()
    {
        Registry r = buildRegistry(AutobuildModule.class);

        StringHolder sh = r.getService(StringHolder.class);

        sh.setValue("Foo");

        assertEquals(sh.getValue(), "Foo");

        r.shutdown();
    }

    @Test
    public void exception_in_autobuild_service_constructor()
    {
        Registry r = buildRegistry(ExceptionInConstructorModule.class);

        Pingable pingable = r.getService(Pingable.class);

        try
        {
            pingable.ping();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking constructor", "ExceptionInConstructorServiceImpl()",
                    "Yes, we have no tomatoes.");
        }

        r.shutdown();
    }

    @Test
    public void non_proxied_service()
    {
        Registry r = buildRegistry(NonProxiedServiceModule.class);

        // Note: obtained via the (or an) interface implemented by
        // the service implementation.

        StringHolder holder = r.getService(StringHolder.class);

        assertTrue(holder instanceof StringHolderImpl);

        r.shutdown();
    }

    @Test
    public void convention_over_configuration_service()
    {
        Registry r = buildRegistry(ConventionModule.class);

        StringHolder holder = r.getService(StringHolder.class);

        holder.setValue("Bar");

        assertEquals(holder.getValue(), "Bar");

        r.shutdown();
    }

    @Test
    public void convention_over_configuration_service_impl_not_found()
    {
        try
        {
            buildRegistry(ConventionModuleImplementationNotFound.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Could not find default implementation class org.apache.tapestry5.ioc.StringTransformerImpl.",
                    "Please provide this class, or bind the service interface to a specific implementation class.");
        }
    }

    @Test
    public void convention_over_configuration_service_wrong_impl_found()
    {
        try
        {
            buildRegistry(ConventionFailureModule.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "No service implements the interface " + Pingable.class.getName());
        }
    }

    @Test
    public void service_builder_method_uses_autobuild()
    {
        Registry r = buildRegistry(ServiceBuilderAutobuilderModule.class);

        StringHolder holder = r.getService(StringHolder.class);

        // Check that it works.

        holder.setValue("Foo");

        assertEquals(holder.getValue(), "Foo");

        r.shutdown();
    }

    @Test
    public void autobuild_via_registry()
    {
        Registry r = buildRegistry();

        StringHolder holder = r.autobuild(StringHolderImpl.class);

        assertSame(holder.getClass(), StringHolderImpl.class);

        // Check that it works.

        holder.setValue("Foo");

        assertEquals(holder.getValue(), "Foo");

        r.shutdown();
    }

    /**
     * TAP5-967
     */
    @Test
    public void autobuild_with_description_via_registry()
    {
        Registry r = buildRegistry();

        StringHolder holder = r.autobuild("Building StringHolderImpl", StringHolderImpl.class);

        assertSame(holder.getClass(), StringHolderImpl.class);

        // Check that it works.

        holder.setValue("Bar");

        assertEquals(holder.getValue(), "Bar");

        r.shutdown();
    }

    @Test
    public void service_builder_method_uses_autobuild_with_failure()
    {
        Registry r = buildRegistry(ServiceBuilderAutobuilderModule.class);

        // We can get the proxy.

        Pingable pingable = r.getService(Pingable.class);

        try
        {
            // But it fails at realization

            pingable.ping();

            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Class org.apache.tapestry5.ioc.UnbuildablePingable does not contain a public constructor needed to autobuild.");
        }

        r.shutdown();
    }

    @Test
    public void autobuild_via_registry_no_constructor()
    {
        Registry r = buildRegistry();

        try
        {
            r.autobuild(UnbuildablePingable.class);

            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Class org.apache.tapestry5.ioc.UnbuildablePingable does not contain a public constructor needed to autobuild.");
        }

        r.shutdown();
    }

    @Test
    public void autobuild_via_registry_constructor_exception()
    {
        Registry r = buildRegistry();

        try
        {
            r.autobuild(FailInConstructorRunnable.class);

            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Error invoking constructor",
                    "org.apache.tapestry5.ioc.FailInConstructorRunnable()",
                    "Failure in Runnable constructor.");
        }

        r.shutdown();
    }

    @Test
    public void get_service_by_unknown_id()
    {
        Registry r = buildRegistry();

        try
        {
            r.getService("PeekABoo", Runnable.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Service id \'PeekABoo\' is not defined by any module.");
        }

        r.shutdown();
    }

    @Test
    public void request_service_by_type_with_no_matches()
    {

        Registry r = buildRegistry();

        try
        {
            r.getService(PreparedStatement.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "No service implements the interface java.sql.PreparedStatement.");
        }

        r.shutdown();
    }

    @Test
    public void request_service_by_type_with_multiple_matches()
    {
        Registry r = buildRegistry(DuplicateServiceTypeModule.class);

        try
        {
            r.getService(Pingable.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Service interface org.apache.tapestry5.ioc.Pingable is matched by 2 services: Barney, Fred.  Automatic dependency resolution requires that exactly one service implement the interface.");
        }

        r.shutdown();
    }

    @Test
    public void service_build_method_return_type_not_interface()
    {
        Registry r = buildRegistry(ConcreteServiceBuilderModule.class);

        StringHolder holder = r.getService(StringHolder.class);

        // No interface means no proxy.

        assertTrue(holder instanceof StringHolderImpl);

        // But the implementation is cached

        assertSame(r.getService(StringHolder.class), holder);

        r.shutdown();
    }

    @Test
    public void symbol_in_inject_annotation_is_expanded()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("Greeter", Greeter.class);

        assertEquals(g.getGreeting(), "Hello");
        assertEquals(g.toString(), "<Proxy for Greeter(org.apache.tapestry5.ioc.Greeter)>");

        r.shutdown();
    }

    @Test
    public void symbol_in_registry_call_for_service_is_expanded()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("${greeter}", Greeter.class);

        assertEquals(g.getGreeting(), "Hello");
        assertEquals(g.toString(), "<Proxy for HelloGreeter(org.apache.tapestry5.ioc.Greeter)>");

        r.shutdown();
    }

    @Test
    public void injection_by_marker_with_single_match()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("InjectedBlueGreeter", Greeter.class);

        assertEquals(g.getGreeting(), "Blue");

        r.shutdown();
    }

    @Test
    public void injection_by_marker_with_multiple_matches()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("InjectedRedGreeter", Greeter.class);

        try
        {
            g.getGreeting();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Unable to locate a single service assignable to type org.apache.tapestry5.ioc.Greeter with marker annotation(s) org.apache.tapestry5.ioc.RedMarker",
                    "org.apache.tapestry5.ioc.GreeterModule.buildRedGreeter1()",
                    "org.apache.tapestry5.ioc.GreeterModule.buildRedGreeter2()");
        }

        r.shutdown();
    }

    @Test
    public void injection_by_marker_with_zero_matches()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("InjectedYellowGreeter", Greeter.class);

        try
        {
            g.getGreeting();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Exception constructing service",
                    "Unable to locate any service assignable to type org.apache.tapestry5.ioc.Greeter with marker annotation(s) org.apache.tapestry5.ioc.YellowMarker.");
        }

        r.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void builtin_services_available_via_marker_annotation()
    {
        Registry r = buildRegistry();

        TypeCoercer tc1 = r.getService("TypeCoercer", TypeCoercer.class);

        Builtin annotation = newMock(Builtin.class);

        AnnotationProvider ap = mockAnnotationProvider();

        train_getAnnotation(ap, Builtin.class, annotation);

        // On the build server, the order of keys inside the RegistryImpl's _markerToServiceDef
        // is different, and so it *may* query ofr a number of other annotation
        // besides Builtin.

        expect(ap.getAnnotation(EasyMock.isA(Class.class))).andStubReturn(null);

        replay();

        TypeCoercer tc2 = r.getObject(TypeCoercer.class, ap);

        assertSame(tc1, tc2);

        verify();

        r.shutdown();
    }

    /**
     * A cursory test for {@link ServiceActivityScoreboard}, just to see if any data has been
     * collected.
     */
    @Test
    public void service_activity_scoreboard()
    {
        Registry r = buildRegistry(GreeterModule.class);

        ServiceActivityScoreboard scoreboard = r.getService(ServiceActivityScoreboard.class);

        // Force the state of a few services.

        TypeCoercer tc = r.getService("TypeCoercer", TypeCoercer.class);

        tc.coerce("123", Integer.class);

        r.getService("BlueGreeter", Greeter.class);

        // Now get the activity list and poke around.

        List<ServiceActivity> serviceActivity = scoreboard.getServiceActivity();

        assertTrue(serviceActivity.size() > 0);

        for (ServiceActivity a : serviceActivity)
        {
            String serviceId = a.getServiceId();

            if (serviceId.equals("ClassFactory"))
                assertEquals(a.getStatus(), Status.BUILTIN);

            if (serviceId.equals("RedGreeter1"))
            {
                assertEquals(a.getStatus(), Status.DEFINED);
                assertEquals(a.getMarkers().contains(BlueMarker.class), false);
                assertEquals(a.getMarkers().contains(RedMarker.class), true);
            }

            if (serviceId.equals("TypeCoercer"))
                assertEquals(a.getStatus(), Status.REAL);

            if (serviceId.equals("BlueGreeter"))
            {
                assertEquals(a.getStatus(), Status.VIRTUAL);
                assertEquals(a.getMarkers().contains(BlueMarker.class), true);
                assertEquals(a.getMarkers().contains(RedMarker.class), false);
            }
        }

        r.shutdown();
    }

    @Test
    public void get_service_by_type_and_markers()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter blue = r.getService(Greeter.class, BlueMarker.class);

        assert blue.getGreeting().equals("Blue");

        r.shutdown();
    }


    @Test
    public void service_activity_scoreboard_perthread() throws InterruptedException
    {
        final Registry r = buildRegistry(GreeterModule.class, PerThreadModule.class);

        ServiceActivityScoreboard scoreboard = r.getService(ServiceActivityScoreboard.class);

        // Force the state of a few services.

        final StringHolder holder = r.getService(StringHolder.class);

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                holder.setValue("barney");
                assertEquals(holder.getValue(), "barney");

                r.cleanupThread();
            }
        };

        Thread t = new Thread(runnable);

        t.start();
        t.join();

        // Now get the activity list and poke around.

        List<ServiceActivity> serviceActivity = scoreboard.getServiceActivity();

        assertTrue(serviceActivity.size() > 0);

        for (ServiceActivity a : serviceActivity)
        {
            String serviceId = a.getServiceId();

            if (serviceId.equals("StringHolder"))
                assertEquals(a.getStatus(), Status.VIRTUAL);
        }

        r.cleanupThread();
        r.shutdown();
    }

    @Test
    public void proxy_autobuild_object()
    {
        Registry r = buildRegistry();

        IntegrationTest.countingGreeterInstantiationCount = 0;

        Greeter g = r.proxy(Greeter.class, CountingGreeterImpl.class);

        assertEquals(IntegrationTest.countingGreeterInstantiationCount, 0);

        assertNotNull(g.toString());

        assertEquals(IntegrationTest.countingGreeterInstantiationCount, 0);

        // Show that the class is not instantiated until a method is invoked, and that its
        // only instantiated once.

        for (int i = 0; i < 5; i++)
        {
            assertEquals(g.getGreeting(), "Hello");
            assertEquals(IntegrationTest.countingGreeterInstantiationCount, 1);
        }

        r.shutdown();
    }

    /**
     * TAPESTRY-2117
     */
    @Test
    public void circular_module_references_are_ignored()
    {
        Registry r = buildRegistry(HelterModule.class);

        Runnable helter = r.getService("Helter", Runnable.class);
        Runnable skelter = r.getService("Skelter", Runnable.class);

        assertNotSame(helter, skelter);

        r.shutdown();
    }

    @Test
    public void bind_to_service_builder()
    {
        Registry r = buildRegistry(ServiceBuilderModule.class);

        Greeter g = r.getService("Greeter", Greeter.class);

        assertEquals(g.getGreeting(), "Greetings from service Greeter.");

        r.shutdown();
    }

    @Test
    public void bind_to_service_binder_that_throws_exception()
    {
        Registry r = buildRegistry(ServiceBuilderModule.class);

        Greeter g = r.getService("BrokenGreeter", Greeter.class);

        try
        {
            g.getGreeting();
            unreachable();
        } catch (Exception ex)
        {
            assertEquals(ex.getMessage(),
                    "Exception constructing service 'BrokenGreeter': Failure inside ServiceBuilder callback.");
        }

        r.shutdown();
    }

    @Test
    public void invalid_class_in_manifest() throws Exception
    {
        File fakejar = new File("src/test/fakejar");

        assertTrue(fakejar.exists() && fakejar.isDirectory(), "src/test/fakejar must be an existing directory");

        URL url = fakejar.toURL();

        URLClassLoader loader = new URLClassLoader(new URL[]
                {url}, Thread.currentThread().getContextClassLoader());

        RegistryBuilder builder = new RegistryBuilder(loader);

        try
        {
            IOCUtilities.addDefaultModules(builder);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Exception loading module(s) from manifest",
                    "Failure loading Tapestry IoC module class does.not.exist.Module");
        }
    }

    @Test
    public void local_annotation()
    {
        Registry r = buildRegistry(GreeterModule.class, LocalModule.class);

        StringHolder g = r.getService("LocalGreeterHolder", StringHolder.class);

        // Comes from the @Local DrawlGreeter, even though there are many other Greeter services
        // available.

        assertEquals(g.getValue(), "Hello, y'all!");

        r.shutdown();
    }

    @Test
    public void decorator_receive_delegate_by_specific_type()
    {
        Registry r = buildRegistry(GreeterModule.class, SpecificDecoratorModule.class);

        Greeter g = r.getService("HelloGreeter", Greeter.class);

        assertEquals(g.getGreeting(), "HELLO");

        r.shutdown();
    }

    @Test
    public void cyclic_dependency_in_MOP() throws Exception
    {
        Registry r = buildRegistry(CyclicMOPModule.class);

        Runnable trigger = r.getService("Trigger", Runnable.class);

        try
        {
            trigger.run();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Construction of service 'TypeCoercer' has failed due to recursion");
        }

        r.shutdown();
    }

    @Test
    public void no_public_constructor_on_module_builder_class()
    {
        Registry r = buildRegistry(PrivateConstructorModule.class);

        try
        {
            r.getService("Trigger", Runnable.class).run();

            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Module class org.apache.tapestry5.ioc.internal.PrivateConstructorModule does not contain any public constructors.");
        }
    }

    @Test
    public void too_many_public_constructors_on_module_builder_class()
    {
        Registry r = buildRegistry(ExtraPublicConstructorsModule.class);

        UpcaseService s = r.getService(UpcaseService.class);

        assertEquals(s.upcase("Hello, ${fred}"), "HELLO, FLINTSTONE");

        r.shutdown();
    }

    @Test
    public void unordered_configuration_contribute_by_class()
    {
        Registry r = buildRegistry(ContributeByClassModule.class);

        StringTransformer st = r.getService("MasterStringTransformer", StringTransformer.class);

        assertEquals(st.transform("Tapestry"), "TAPESTRY");

        r.shutdown();
    }

    @Test
    public void ordered_configuration_contribute_by_class()
    {
        Registry r = buildRegistry(ContributeByClassModule.class);

        StringTransformer st = r.getService("StringTransformerChain", StringTransformer.class);

        assertEquals(st.transform("Tapestry"), "TAPESTRY");

        r.shutdown();
    }

    @Test
    public void mapped_configuration_contribute_by_class()
    {
        Registry r = buildRegistry(ContributeByClassModule.class);

        StringTransformer st = r.getService("MappedStringTransformer", StringTransformer.class);

        assertEquals(st.transform("Tapestry"), "TAPESTRY");

        r.shutdown();
    }

    /**
     * TAP5-139
     */
    @Test
    public void autobuild_injection()
    {
        Registry r = buildRegistry(AutobuildInjectionModule.class);

        StringTransformer st = r.getService(StringTransformer.class);

        assertEquals(st.transform("Hello, ${fred}"), "Hello, flintstone");

        r.shutdown();
    }

    /**
     * TAP5-292
     */
    @Test
    public void field_resource_injection()
    {
        Registry r = buildRegistry(FieldResourceInjectionModule.class);

        FieldResourceService s = r.getService(FieldResourceService.class);

        assertEquals(s.getServiceId(), "FieldResourceService");
        assertListsEquals(s.getLabels(), "Barney", "Betty", "Fred", "Wilma");

        r.shutdown();
    }

    /**
     * TAP5-291
     */
    @Test
    public void post_injection_method_invoked()
    {
        Registry r = buildRegistry(PostInjectionMethodModule.class);

        Greeter g = r.getService(Greeter.class);

        assertEquals(g.getGreeting(), "Greetings from ServiceIdGreeter.");
    }

    /**
     * TAP5-429
     */
    @Test
    public void contribute_to_unknown_service()
    {
        try
        {
            buildRegistry(InvalidContributeDefModule.class);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertMessageContains(
                    ex,
                    "Contribution org.apache.tapestry5.ioc.InvalidContributeDefModule.contributeDoesNotExist(Configuration)",
                    "is for service 'DoesNotExist', which does not exist.");
        }
    }

    /**
     * TAP5-436
     */
    @Test
    public void extra_methods_on_module_class_are_errors()
    {
        try
        {
            buildRegistry(ExtraMethodsModule.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Module class org.apache.tapestry5.ioc.ExtraMethodsModule contains unrecognized public methods: ",
                    "thisMethodIsInvalid()", "soIsThisMethod().");
        }
    }

    /**
     * TAP5-430
     */
    @Test
    public void service_builder_method_marked_for_no_decoration()
    {
        Registry r = buildRegistry(PreventDecorationModule.class);

        StringTransformer st = r.getService(StringTransformer.class);

        assertEquals(st.transform("tapestry"), "TAPESTRY");

        r.shutdown();
    }

    /**
     * TAP5-430
     */
    @Test
    public void bind_service_marked_for_no_decoration_explicitly()
    {
        Registry r = buildRegistry(PreventDecorationModule.class);

        Greeter g = r.getService(Greeter.class);

        assertEquals(g.getGreeting(), "Greetings from ServiceIdGreeter.");

        r.shutdown();
    }

    /**
     * TAP5-430
     */
    @Test
    public void bind_service_with_prevent_service_decoration_annotations_on_implementation_class()
    {
        Registry r = buildRegistry(PreventDecorationModule.class);

        Rocket rocket = r.getService(Rocket.class);

        assertEquals(rocket.getCountdown(), "3, 2, 1, Launch!");

        r.shutdown();
    }

    /**
     * TAP5-437
     */
    @Test
    public void successful_ordered_configuration_override()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class, ConfigurationOverrideModule.class);

        NameListHolder service = r.getService("OrderedNames", NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("BARNEY", "WILMA", "Mr. Flintstone"));
    }

    /**
     * TAP5-437
     */
    @Test
    public void failed_ordered_configuration_override()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class, FailedConfigurationOverrideModule.class);

        NameListHolder service = r.getService("OrderedNames", NameListHolder.class);

        try
        {
            service.getNames();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Failure processing override from org.apache.tapestry5.ioc.FailedConfigurationOverrideModule.contributeOrderedNames(OrderedConfiguration)",
                    "Override for object 'wilma' is invalid as it does not match an existing object.");
        }
    }

    /**
     * TAP5-437
     */
    @Test
    public void duplicate_ordered_configuration_override()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class, ConfigurationOverrideModule.class,
                DuplicateConfigurationOverrideModule.class);

        NameListHolder service = r.getService("OrderedNames", NameListHolder.class);

        try
        {
            service.getNames();
            unreachable();
        } catch (RuntimeException ex)
        {
            // Can't get too specific since we don't know which module will get processed first
            assertMessageContains(ex, "Error invoking service contribution method ",
                    "Contribution 'fred' has already been overridden");
        }
    }

    /**
     * TAP5-437
     */
    @Test
    public void mapped_configuration_override()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class, ConfigurationOverrideModule.class);

        StringLookup sl = r.getService(StringLookup.class);

        // Due to override wilma to null:

        assertListsEquals(sl.keys(), "barney", "betty", "fred");

        assertEquals(sl.lookup("fred"), "Mr. Flintstone");
    }

    /**
     * TAP5-437
     */
    @Test
    public void nonmatching_mapped_configuration_override()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class,
                NonmatchingMappedConfigurationOverrideModule.class);

        StringLookup sl = r.getService(StringLookup.class);

        try
        {
            sl.keys();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Override for key alley cat (at org.apache.tapestry5.ioc.util.NonmatchingMappedConfigurationOverrideModule.contributeStringLookup(MappedConfiguration)",
                    "does not match an existing key.");
        }
    }

    /**
     * TAP-437
     */
    @Test
    public void duplicate_override_for_mapped_configuration()
    {
        Registry r = buildRegistry(FredModule.class, BarneyModule.class, ConfigurationOverrideModule.class,
                DuplicateConfigurationOverrideModule.class);

        StringLookup sl = r.getService(StringLookup.class);

        try
        {
            sl.keys();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking service contribution method",
                    "Contribution key fred has already been overridden");
        }
    }

    /**
     * TAP5-316
     */
    @Test
    public void service_override()
    {
        Registry r = buildRegistry(GreeterServiceOverrideModule.class);

        Greeter g = r.getObject(Greeter.class, null);

        assertEquals(g.getGreeting(), "Override Greeting");
    }

    /**
     * TAP5-60
     */
    @Test
    public void non_void_advisor_method_is_error()
    {
        try
        {
            buildRegistry(NonVoidAdvisorMethodModule.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Advise method org.apache.tapestry5.ioc.NonVoidAdvisorMethodModule.adviseFoo(MethodAdviceReceiver)",
                    "does not return void.");
        }
    }

    /**
     * TAP5-60
     */
    @Test
    public void advisor_methods_must_take_a_method_advisor_parameter()
    {
        try
        {
            buildRegistry(AdviceMethodMissingAdvisorParameterModule.class);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                    "Advise method org.apache.tapestry5.ioc.AdviceMethodMissingAdvisorParameterModule.adviseBar()",
                    "must take a parameter of type org.apache.tapestry5.ioc.MethodAdviceReceiver.");
        }
    }

    /**
     * TAP5-60
     */
    @Test
    public void advise_services()
    {
        Registry r = buildRegistry(AdviceDemoModule.class);

        Greeter g = r.getService(Greeter.class);

        assertEquals(g.getGreeting(), "ADVICE IS EASY!");
    }

    @Test
    public void contribute_by_annotation()
    {
        Registry r = buildRegistry(AlphabetModule.class, AlphabetModule2.class);

        NameListHolder greek = r.getService("Greek", NameListHolder.class);

        assertListsEquals(greek.getNames(), "Alpha", "Beta", "Gamma", "Delta");

        NameListHolder anotherGreek = r.getService("AnotherGreek", NameListHolder.class);

        assertListsEquals(anotherGreek.getNames(), "Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        NameListHolder hebrew = r.getService("Hebrew", NameListHolder.class);

        assertListsEquals(hebrew.getNames(), "Alef", "Bet", "Gimel", "Dalet", "He", "Vav");

        NameListHolder2 holder = r.getService("ServiceWithEmptyConfiguration", NameListHolder2.class);

        assertEquals(holder.getNames(), Arrays.asList());

        r.shutdown();

    }

    @Test
    public void contribute_by_annotation_to_nonexistent_service()
    {
        try
        {
            buildRegistry(InvalidContributeDefModule2.class);
            unreachable();
        } catch (Exception e)
        {
            assertMessageContains(
                    e,
                    "Contribution org.apache.tapestry5.ioc.InvalidContributeDefModule2.provideConfiguration(OrderedConfiguration)",
                    "is for service 'interface org.apache.tapestry5.ioc.NameListHolder'",
                    "qualified with marker annotations [", "interface org.apache.tapestry5.ioc.BlueMarker",
                    "interface org.apache.tapestry5.ioc.RedMarker", "], which does not exist.");
        }
    }

    @Test
    public void contribute_by_annotation_wrong_marker()
    {
        try
        {
            buildRegistry(InvalidContributeDefModule3.class);
            unreachable();
        } catch (Exception e)
        {
            assertMessageContains(
                    e,
                    "Contribution org.apache.tapestry5.ioc.InvalidContributeDefModule3.provideConfiguration(OrderedConfiguration)",
                    "is for service 'interface org.apache.tapestry5.ioc.NameListHolder'",
                    "qualified with marker annotations [interface org.apache.tapestry5.ioc.BlueMarker], which does not exist.");
        }
    }

    @Test
    public void service_resources_available_when_invoking_contribute_method()
    {
        Registry r = buildRegistry(InjectionCheckModule.class);

        InjectionCheck service = r.getService(InjectionCheck.class);

        assertSame(service.getLogger(), service.getValue("logger"));

        IndirectResources il = (IndirectResources) service.getValue("indirect-resources");

        assertSame(service.getLogger(), il.getLogger());
        assertSame(service.getLogger(), il.getResources().getLogger());

        r.shutdown();
    }

    @Test
    public void advise_by_annotation()
    {
        Registry r = buildRegistry(GreeterModule2.class, AdviseByMarkerModule.class);

        Greeter green = r.getService("GreenGreeter", Greeter.class);

        assertEquals(green.getGreeting(), "gamma[beta[alpha[Green]]]");

        r.shutdown();

    }

    @Test
    public void advise_by_locale_annotation()
    {
        Registry r = buildRegistry(GreeterModule2.class, AdviseByMarkerModule.class);

        Greeter red = r.getService("RedGreeter", Greeter.class);

        assertEquals(red.getGreeting(), "delta[Red]");

        r.shutdown();

    }

    @Test
    public void advise_by_annotation_matched_by_id()
    {
        Registry r = buildRegistry(AdviseByMarkerModule2.class);

        Greeter green = r.getService("RedGreeter", Greeter.class);

        assertEquals(green.getGreeting(), "beta[alpha[Red]]");

        r.shutdown();

    }

    @Test
    public void decorate_by_annotation()
    {
        Registry r = buildRegistry(GreeterModule2.class, DecorateByMarkerModule.class);

        Greeter green = r.getService("GreenGreeter", Greeter.class);

        assertEquals(green.getGreeting(), "Decorated by foo[Decorated by baz[Decorated by bar[Green]]]");

        r.shutdown();

    }

    @Test
    public void decorate_by_locale_annotation()
    {
        Registry r = buildRegistry(GreeterModule2.class, DecorateByMarkerModule.class);

        Greeter red = r.getService("RedGreeter", Greeter.class);

        assertEquals(red.getGreeting(), "Decorated by barney[Red]");

        r.shutdown();

    }

    @Test
    public void decorate_by_annotation_matched_by_id()
    {
        Registry r = buildRegistry(DecorateByMarkerModule2.class);

        Greeter green = r.getService("RedGreeter", Greeter.class);

        assertEquals(green.getGreeting(), "Decorated by beta[Decorated by alpha[Red]]");

        r.shutdown();

    }

    @Test
    public void startup_inside_module()
    {
        Registry r = buildRegistry(StartupModule2.class);

        assertFalse(StartupModule2.staticStartupInvoked);

        assertFalse(StartupModule2.instanceStartupInvoked);

        r.performRegistryStartup();

        assertTrue(StartupModule2.staticStartupInvoked);

        assertTrue(StartupModule2.instanceStartupInvoked);

        r.shutdown();
    }

    @Test
    public void case_ignored_in_service_id_of_contribute_method()
    {
        Registry r = buildRegistry(CaseInsensitiveContributeMethodModule.class);

        SymbolSource symbolSource = r.getService(SymbolSource.class);

        assertEquals(symbolSource.valueForSymbol("it"), "works");

        r.shutdown();
    }

    @Test
    public void contributed_values_may_be_coerced_to_correct_type()
    {
        Registry r = buildRegistry(ContributedValueCoercionModule.class);

        SymbolSource source = r.getService(SymbolSource.class);

        assertEquals(source.valueForSymbol("bool-true"), "true");
        assertEquals(source.valueForSymbol("bool-false"), "false");
        assertEquals(source.valueForSymbol("num-12345"), "12345");
    }

    /**
     * TAP5-1674
     */
    @Test
    public void no_implemention_class_defined_for_ServiceBinder_withSimpleId()
    {
        try
        {
            buildRegistry(NoImplementationClassForSimpleIdModule.class);
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "No defined implementation class to generate simple id from");
        }
    }

    @Test
    public void optional_contribution_to_unknown_service_is_not_an_error()
    {

        buildRegistry(OptionalContributionModule.class);
    }
}
