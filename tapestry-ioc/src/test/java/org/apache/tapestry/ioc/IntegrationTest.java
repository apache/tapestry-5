// Copyright 2006 The Apache Software Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void static_builder_method_does_not_instantiate_builder()
    {
        StaticModule.setInstantiated(false);
        StaticModule.setFredRan(false);

        Registry r = buildRegistry(StaticModule.class);

        Runnable fred = r.getService("static.Fred", Runnable.class);

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

        Runnable fred = r.getService("static.Barney", Runnable.class);

        fred.run();

        assertFalse(StaticModule.isInstantiated());
        assertTrue(StaticModule.getDecoratorRan());
    }

    @Test
    public void static_contributor_method_does_not_instantiate_builder()
    {
        StaticModule.setInstantiated(false);

        Registry r = buildRegistry(StaticModule.class);

        NameListHolder holder = r.getService("static.Names", NameListHolder.class);

        List<String> names = holder.getNames();

        assertEquals(names, Arrays.asList("Fred"));

        assertFalse(StaticModule.isInstantiated());
    }

    @Test
    public void shutdown_deactivates_proxies()
    {
        Registry r = buildRegistry();

        Runnable service = r.getService("fred.Fred", Runnable.class);

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
                    "Proxy for service fred.Fred is no longer active because the IOC Registry has been shut down.");
        }

        // Show that toString() still works, even for a shutdown proxy.

        assertEquals(service.toString(), "<Proxy for fred.Fred(java.lang.Runnable)>");
    }

    /**
     * Along the way, we also test a few other things, such as decorator matching and automatic
     * dependency resolution.
     */
    @Test
    public void public_service_decorator_order()
    {
        Registry r = buildRegistry();

        Runnable service = r.getService("fred.Fred", Runnable.class);

        // Force creation

        service.run();

        List<String> names = r.getService(DecoratorList.class).getNames();

        // Note that the order of invocation appears backwards, since we build back-to-front

        assertEquals(names, Arrays.asList("gamma", "beta", "alpha"));
    }

    /**
     * Along the way, we are demonstrating that decorators can target multiple services within a
     * module, and can target services in other modules. The main point, though, is the difference
     * between a private and a public service in terms of decoration.
     */
    @Test
    public void private_service_decorator_order()
    {
        Registry r = buildRegistry();

        Runnable service = r.getService("fred.PrivateFredAlias", Runnable.class);

        // Force creation

        service.run();

        List<String> names = r.getService(DecoratorList.class).getNames();

        // The trick here is that the public service (PrivateFredAlias) was decorated first with the
        // full set (the same as the previous test), then the private service (PrivateFred) was
        // decorated just with decorators from module fred.

        assertEquals(names, Arrays.asList("gamma", "beta", "alpha", "beta", "alpha"));
    }

    @Test
    public void public_service_unordered_configuration()
    {
        Registry r = buildRegistry();

        NameListHolder service = r.getService("fred.UnorderedNames", NameListHolder.class);

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

        NameListHolder service = r.getService("fred.OrderedNames", NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("BARNEY", "FRED"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void service_mapped_configuration()
    {
        Registry r = buildRegistry();

        Sizer sizer = r.getService("barney.Sizer", Sizer.class);

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
    public void private_service_unordered_configuration()
    {
        Registry r = buildRegistry();

        NameListHolder service = r.getService(
                "fred.PrivateUnorderedNamesAlias",
                NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("Omega", "PrivateUnorderedNames"));
    }

    @Test
    public void unknown_lifecycle()
    {
        Registry r = buildRegistry(UnknownLifecycleModule.class);

        try
        {
            r.getService("ioc.test.UnknownLifecycle", Runnable.class);
            unreachable();
        }
        catch (Exception ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Error building service proxy for service 'ioc.test.UnknownLifecycle' "
                            + "(at org.apache.tapestry.ioc.UnknownLifecycleModule.buildUnknownLifecycle()): "
                            + "Unknown service lifecycle 'magic'.");
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
    public void use_of_service_object_provider_prefix()
    {
        Registry r = buildRegistry(ServiceObjectProviderModule.class);

        StringTransformer st = r.getObject(
                "service:ioc.test.PublicTransformer",
                StringTransformer.class);

        // The PublicTransform gets the PrivateTransformer via
        // an @Inject.

        assertEquals(st.transform("fred"), "FRED");
    }

    @Test
    public void recursive_module_construction_is_caught()
    {
        Registry r = buildRegistry(RecursiveConstructorModule.class);

        try
        {
            Runnable runnable = r.getService("recursive.Runnable", Runnable.class);

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
        assertFalse(EagerLoadModule._eagerLoadDidHappen);

        Registry r = buildRegistry(EagerLoadModule.class);

        // Prevents warning: r is never read
        assertNotNull(r);

        assertTrue(EagerLoadModule._eagerLoadDidHappen);

        r = null;
    }

    @Test
    public void inject_service_annotation_with_symbol()
    {
        Registry r = buildRegistry(IndirectionModule.class);

        Indirection outer = r.getService("indirection.Outer", Indirection.class);

        assertEquals(outer.getName(), "OUTER[INNER]");
    }

    @Test
    public void inject_annotation_with_symbol()
    {
        Registry r = buildRegistry(IndirectionModule.class);

        Indirection outer = r.getService("indirection.Outer2", Indirection.class);

        assertEquals(outer.getName(), "OUTER2[INNER]");
    }

    @Test
    public void registry_get_service_with_symbol()
    {
        Registry r = buildRegistry(IndirectionModule.class);

        Indirection inner = r.getService("${indirection.inner}", Indirection.class);

        assertEquals(inner.getName(), "INNER");
    }

    @Test
    public void registry_get_object_with_symbol()
    {
        Registry r = buildRegistry(IndirectionModule.class);

        Indirection inner = r.getObject("${indirection.object-inner}", Indirection.class);

        assertEquals(inner.getName(), "INNER");
    }

    @Test
    public void inject_annotation_literal_value()
    {
        Registry r = buildRegistry(InjectLiteralModule.class);

        IntHolder holder = r.getService(IntHolder.class);

        assertEquals(holder.getValue(), 42);
    }
}
