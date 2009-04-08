// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.Future;

public class NonParallelExecutorTest extends TestBase
{
    private Registry registry;

    private ParallelExecutor executor;

    @BeforeClass
    public void setup()
    {
        RegistryBuilder builder = new RegistryBuilder();
        builder.add(NonParallelModule.class);

        registry = builder.build();
        executor = registry.getService(ParallelExecutor.class);
    }

    @AfterClass
    public void cleanup()
    {
        registry.shutdown();

        registry = null;
        executor = null;
    }

    @Test
    public void invoke_proxy()
    {
        Invokable<String> inv = newMock(Invokable.class);

        String value = "invokable-value";

        expect(inv.invoke()).andReturn(value);

        replay();

        assertSame(executor.invoke(String.class, inv), value);

        verify();
    }

    @Test
    public void invoke_with_future() throws Exception
    {
        Invokable<String> inv = newMock(Invokable.class);

        String value = "invokable-value";

        expect(inv.invoke()).andReturn(value);

        replay();

        Future<String> future = executor.invoke(inv);

        assertFalse(future.cancel(true));
        assertFalse(future.isCancelled());
        assertTrue(future.isDone());
        assertSame(future.get(), value);
        assertSame(future.get(0, null), value);

        verify();

    }
}


