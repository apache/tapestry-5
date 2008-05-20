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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class ChainBuilderImplTest extends IOCInternalTestCase
{
    private final ChainBuilder builder = new ChainBuilderImpl(new ClassFactoryImpl());

    @Test
    public void simple_void_method()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        // Training:

        r1.run();
        r2.run();

        replay();

        Runnable chain = build(Runnable.class, r1, r2);

        chain.run();

        verify();

        Assert.assertEquals(chain.toString(), "<Command chain of java.lang.Runnable>");
    }

    @Test
    public void int_method()
    {
        ChainCommand c1 = mockChainCommand();
        ChainCommand c2 = mockChainCommand();

        expect(c1.workInt(7)).andReturn(0);

        expect(c2.workInt(7)).andReturn(99);

        replay();

        ChainCommand chain = build(ChainCommand.class, c1, c2);

        assertEquals(chain.workInt(7), 99);

        verify();
    }

    @Test
    public void int_method_shortcircuits()
    {
        ChainCommand c1 = mockChainCommand();
        ChainCommand c2 = mockChainCommand();

        expect(c1.workInt(7)).andReturn(88);

        replay();

        ChainCommand chain = build(ChainCommand.class, c1, c2);

        assertEquals(chain.workInt(7), 88);

        verify();
    }

    @Test
    public void boolean_method()
    {
        ChainCommand c1 = mockChainCommand();
        ChainCommand c2 = mockChainCommand();

        train_workBoolean(c1, true, false);
        train_workBoolean(c2, true, true);

        replay();

        ChainCommand chain = build(ChainCommand.class, c1, c2);

        assertEquals(chain.workBoolean(true), true);

        verify();
    }

    protected final void train_workBoolean(ChainCommand command, boolean parameter, boolean result)
    {
        expect(command.workBoolean(parameter)).andReturn(result);
    }

    @Test
    public void string_method()
    {
        ChainCommand c1 = mockChainCommand();
        ChainCommand c2 = mockChainCommand();

        expect(c1.workString("fred")).andReturn(null);

        expect(c2.workString("fred")).andReturn("flintstone");

        replay();

        ChainCommand chain = build(ChainCommand.class, c1, c2);

        assertEquals(chain.workString("fred"), "flintstone");

        verify();

    }

    @Test
    public void double_method()
    {
        ChainCommand c1 = mockChainCommand();
        ChainCommand c2 = mockChainCommand();

        expect(c1.workDouble(1.2d)).andReturn(0d);

        expect(c2.workDouble(1.2d)).andReturn(3.14d);

        replay();

        ChainCommand chain = build(ChainCommand.class, c1, c2);

        assertEquals(chain.workDouble(1.2d), 3.14d);

        verify();
    }

    private ChainCommand mockChainCommand()
    {
        return newMock(ChainCommand.class);
    }

    @Test
    public void fabricated_classes_are_reused()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        Runnable chain1 = build(Runnable.class, r1);
        Runnable chain2 = build(Runnable.class, r2);

        Assert.assertSame(chain1.getClass(), chain2.getClass());

        // Now make sure that the two instances are independent.

        r1.run();

        replay();

        chain1.run();

        verify();

        r2.run();

        replay();

        chain2.run();

        verify();
    }

    private <T> T build(Class<T> commandInterface, T... commands)
    {
        List<T> list = Arrays.asList(commands);

        return builder.build(commandInterface, list);
    }

}
