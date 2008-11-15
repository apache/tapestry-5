// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.internal.*;
import org.apache.tapestry5.ioc.services.*;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * A few tests that are easiest (or even just possible) by building a Registry and trying out a few things.
 */
public class IntegrationTest extends IOCInternalTestCase
{
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
        }
        catch (RuntimeException ex)
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
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(),
                         "Proxy for service Fred is no longer active because the IOC Registry has been shut down.");
        }

        // Show that toString() still works, even for a shutdown proxy.

        assertEquals(service.toString(), "<Proxy for Fred(java.lang.Runnable)>");
    }

    /**
     * Along the way, we also test a few other things, such as decorator matching and automatic dependency resolution.
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
     * We don't have to do as many public/private etc. tests for the other types of configuration, because the code
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
        }
        catch (Exception ex)
        {
            assertMessageContains(ex, "Exception constructing service 'UnknownScope'", "Unknown service scope 'magic'");
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
     * This test fails at times and I'm not sure why. It's some kind of interaction with other tests but hard to figure
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
        }
        catch (RuntimeException ex)
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking constructor",
                                  "ExceptionInConstructorServiceImpl() (at ExceptionInConstructorServiceImpl.java",
                                  "for service 'Pingable'", "Yes, we have no tomatoes.");
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "No service implements the interface " + StringTransformer.class.getName() + ". Please provide");
        }
    }

    @Test
    public void convention_over_configuration_service_wrong_impl_found()
    {
        try
        {
            buildRegistry(ConventionFailureModule.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "No service implements the interface " + Pingable.class.getName());
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Class org.apache.tapestry5.ioc.UnbuildablePingable does not contain a public constructor needed to autobuild.");

            // Like to check that the message includes the source location

            assertTrue(ex.getMessage().matches(".*\\(at ServiceBuilderAutobuilderModule.java:\\d+\\).*"));
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
        }
        catch (RuntimeException ex)
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking constructor org.apache.tapestry5.ioc.FailInConstructorRunnable()",
                                  "Failure in Runnable constructor.");

            // Like to check that the message includes the source location

            assertTrue(ex.getMessage().matches(".*\\(at FailInConstructorRunnable.java:\\d+\\).*"));
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
        }
        catch (RuntimeException ex)
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
        }
        catch (RuntimeException ex)
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
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking service builder method",
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Error invoking service builder method",
                                  " Unable to locate any service assignable to type org.apache.tapestry5.ioc.Greeter with marker annotation(s) org.apache.tapestry5.ioc.YellowMarker.");
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
     * A cursory test for {@link ServiceActivityScoreboard}, just to see if any data has been collected.
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

            if (serviceId.equals("ClassFactory")) assertEquals(a.getStatus(), Status.BUILTIN);

            if (serviceId.equals("RedGreeter1")) assertEquals(a.getStatus(), Status.DEFINED);

            if (serviceId.equals("TypeCoercer")) assertEquals(a.getStatus(), Status.REAL);

            if (serviceId.equals("BlueGreeter")) assertEquals(a.getStatus(), Status.VIRTUAL);
        }

        r.shutdown();
    }

    @Test
    public void proxy_autobuild_object()
    {
        Registry r = buildRegistry();

        CountingGreeterImpl.instantiationCount = 0;

        Greeter g = r.proxy(Greeter.class, CountingGreeterImpl.class);

        assertEquals(CountingGreeterImpl.instantiationCount, 0);

        assertEquals(g.toString(),
                     "<Autobuild proxy org.apache.tapestry5.ioc.CountingGreeterImpl(org.apache.tapestry5.ioc.Greeter)>");

        assertEquals(CountingGreeterImpl.instantiationCount, 0);

        // Show that the class is not instantiated until a method is invoked, and that its
        // only instantiated once.

        for (int i = 0; i < 5; i++)
        {
            assertEquals(g.getGreeting(), "Hello");
            assertEquals(CountingGreeterImpl.instantiationCount, 1);
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
        }
        catch (Exception ex)
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

        URLClassLoader loader = new URLClassLoader(new URL[] {url}, Thread.currentThread().getContextClassLoader());

        RegistryBuilder builder = new RegistryBuilder(loader);

        try
        {
            IOCUtilities.addDefaultModules(builder);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Exception loading module(s) from manifest",
                                  "Failure loading Tapestry IoC module class does.not.exist.Module"
            );
        }
    }

    @Test
    public void local_annotation()
    {
        Registry r = buildRegistry(GreeterModule.class, LocalModule.class);

        StringHolder g = r.getService("LocalGreeterHolder", StringHolder.class);

        // Comes from the @Local DrawlGreeter, even though there are many other Greeter services available.

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
        }
        catch (RuntimeException ex)
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
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Module builder class org.apache.tapestry5.ioc.internal.PrivateConstructorModule does not contain any public constructors.");
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
}
