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

package org.apache.tapestry.ioc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.internal.ExceptionInConstructorModule;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A few tests that are easiest (or even just possible) by building a Registry and trying out a few
 * things.
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
            assertEquals(
                    ex.getMessage(),
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
     * because the code paths are so similar.
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
            assertMessageContains(
                    ex,
                    "Exception constructing service 'UnknownScope'",
                    "Unknown service scope 'magic'");
        }
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
    }

    /**
     * This test fails at times and I'm not sure why. It's some kind of interaction with other tests
     * but hard to figure out. Damn ThreadLocals!
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
    }

    @Test
    public void eager_service_loading()
    {
        Registry r = buildRegistry(EagerLoadModule.class);

        assertFalse(
                EagerLoadModule._eagerLoadDidHappen,
                "EagerLoadModule is not in correct initial state.");

        r.eagerLoadServices();

        assertTrue(EagerLoadModule._eagerLoadDidHappen);
    }

    @Test
    public void access_to_services_ignores_case()
    {
        Registry r = buildRegistry(FredModule.class);

        Runnable fred = r.getService("Fred", Runnable.class);

        assertSame(r.getService("FRED", Runnable.class), fred);
    }

    @Test
    public void simple_autobuild()
    {
        Registry r = buildRegistry(AutobuildModule.class);

        StringHolder sh = r.getService(StringHolder.class);

        sh.setValue("Foo");

        assertEquals(sh.getValue(), "Foo");
    }

    @Test
    public void exception_in_autobuild_service_constructor()
    {
        Registry r = buildRegistry(ExceptionInConstructorModule.class);

        Runnable runnable = r.getService(Runnable.class);

        try
        {
            runnable.run();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Error invoking constructor",
                    "ExceptionInConstructorServiceImpl() (at ExceptionInConstructorServiceImpl.java",
                    "for service 'Runnable'",
                    "Yes, we have no tomatoes.");
        }
    }

    @Test
    public void non_proxied_service()
    {
        Registry r = buildRegistry(NonProxiedServiceModule.class);

        // Note: obtained via the (or an) interface implemented by
        // the service implementation.

        StringHolder holder = r.getService(StringHolder.class);

        assertTrue(holder instanceof StringHolderImpl);
    }

    @Test
    public void service_builder_method_uses_autobuild()
    {
        Registry r = buildRegistry(ServiceBuilderAutobuilderModule.class);

        StringHolder holder = r.getService(StringHolder.class);

        // Check that it works.

        holder.setValue("Foo");

        assertEquals(holder.getValue(), "Foo");
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
    }

    @Test
    public void service_builder_method_uses_autobuild_with_failure()
    {
        Registry r = buildRegistry(ServiceBuilderAutobuilderModule.class);

        // We can get the proxy.

        Runnable runnable = r.getService(Runnable.class);

        try
        {
            // But it fails at realization

            runnable.run();

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Class org.apache.tapestry.ioc.UnbuildableRunnable does not contain a public constructor needed to autobuild.");

            // Like to check that the message includes the source location

            assertTrue(ex.getMessage().matches(
                    ".*\\(at ServiceBuilderAutobuilderModule.java:\\d+\\).*"));
        }
    }

    @Test
    public void autobuild_via_registry_no_constructor()
    {
        Registry r = buildRegistry();

        try
        {
            r.autobuild(UnbuildableRunnable.class);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Class org.apache.tapestry.ioc.UnbuildableRunnable does not contain a public constructor needed to autobuild.");
        }
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
            assertMessageContains(
                    ex,
                    "Error invoking constructor org.apache.tapestry.ioc.FailInConstructorRunnable()",
                    "Failure in Runnable constructor.");

            // Like to check that the message includes the source location

            assertTrue(ex.getMessage().matches(".*\\(at FailInConstructorRunnable.java:\\d+\\).*"));
        }
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
            assertEquals(
                    ex.getMessage(),
                    "No service implements the interface java.sql.PreparedStatement.");
        }
    }

    @Test
    public void request_service_by_type_with_multiple_matches()
    {
        Registry r = buildRegistry(DuplicateServiceTypeModule.class);

        try
        {
            r.getService(Runnable.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Service interface java.lang.Runnable is matched by 2 services: Barney, Fred.  Automatic dependency resolution requires that exactly one service implement the interface.");
        }
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
    }

    @Test
    public void symbol_in_inject_annotation_is_expanded()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("Greeter", Greeter.class);

        assertEquals(g.getGreeting(), "Hello");
        assertEquals(g.toString(), "<Proxy for Greeter(org.apache.tapestry.ioc.Greeter)>");
    }

    @Test
    public void symbol_in_registry_call_for_service_is_expanded()
    {
        Registry r = buildRegistry(GreeterModule.class);

        Greeter g = r.getService("${greeter}", Greeter.class);

        assertEquals(g.getGreeting(), "Hello");
        assertEquals(g.toString(), "<Proxy for HelloGreeter(org.apache.tapestry.ioc.Greeter)>");
    }

}
