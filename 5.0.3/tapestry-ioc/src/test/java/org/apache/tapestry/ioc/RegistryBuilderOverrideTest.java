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

import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.test.TestBase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class RegistryBuilderOverrideTest extends TestBase
{
    private RegistryBuilder builder;

    private Registry registry;

    public interface Transformer<T>
    {
        T transform(T input);
    }

    public static class IdentifyTransformer<T> implements Transformer<T>
    {
        public T transform(T input)
        {
            return input;
        }
    }

    public static class UppercaseTransformer implements Transformer<String>
    {
        public String transform(String input)
        {
            return input.toUpperCase();
        }
    }

    public static class TestModule
    {
        public static Transformer buildService1()
        {
            return new IdentifyTransformer<String>();
        }

        public static Transformer buildService2()
        {
            return new IdentifyTransformer<String>();
        }

        // Just a proxy for Service2.
        public static Transformer buildService3(@InjectService("Service2")
        Transformer s2)
        {
            return s2;
        }
    }

    @BeforeMethod
    public void before()
    {
        builder = new RegistryBuilder();
        builder.add(TestModule.class);
    }

    @AfterMethod
    public void after()
    {
        if (registry != null)
        {
            registry.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void service_override()
    {
        builder.addServiceOverride("Service2", new UppercaseTransformer());
        registry = builder.build();
        Transformer<String> s1 = registry.getService("Service1", Transformer.class);
        assertEquals(s1.transform("a"), "a");

        Transformer<String> s2 = registry.getService("Service2", Transformer.class);
        assertEquals(s2.transform("a"), "A");
    }

    @Test
    public void overidden_service_injected_into_other_service()
    {
        builder.addServiceOverride("Service2", new UppercaseTransformer());
        registry = builder.build();
        @SuppressWarnings("unchecked")
        Transformer<String> s3 = registry.getService("Service3", Transformer.class);

        assertEquals(s3.transform("a"), "A");
    }

    @Test
    public void overridden_service_with_incorrect_interface_causes_exception()
    {
        builder = new RegistryBuilder();
        builder.add(TestModule.class);
        builder.addServiceOverride("Service2", "bad impl");
        registry = builder.build();
        try
        {
            registry.getService("Service2", Transformer.class);

            unreachable();
        }
        catch (RuntimeException e)
        {
            String errorMsg = e.getMessage();
            assertTrue(errorMsg.contains("String"));
            assertTrue(errorMsg.contains("Transformer"));
        }
    }
}
