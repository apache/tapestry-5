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
import org.apache.tapestry5.ioc.StringHolder;
import org.apache.tapestry5.ioc.StringHolderImpl;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.testng.annotations.Test;

import java.util.List;

public class ParallelExecutorTest extends IOCInternalTestCase
{
    @Test
    public void thunk_creation()
    {
        int count = 100;

        List<StringHolder> thunks = CollectionFactory.newList();

        ParallelExecutor parallelExecutor = getService(ParallelExecutor.class);

        for (int i = 0; i < count; i++)
        {
            final String value = String.format("Value[%d]", i);

            Invokable<StringHolder> inv = new Invokable<StringHolder>()
            {
                public StringHolder invoke()
                {
                    StringHolder holder = new StringHolderImpl();
                    holder.setValue(value);

                    return holder;
                }
            };

            thunks.add(parallelExecutor.invoke(StringHolder.class, inv));
        }

        for (int j = 0; j < 2; j++)
        {
            for (int i = 0; i < count; i++)
            {
                assertEquals(thunks.get(i).getValue(), String.format("Value[%d]", i));
            }
        }
    }

    @Test
    public void exception_thrown_by_invocation()
    {
        ParallelExecutor parallelExecutor = getService(ParallelExecutor.class);

        Invokable<StringHolder> inv = new Invokable<StringHolder>()
        {
            public StringHolder invoke()
            {
                throw new RuntimeException("Future failure!");
            }
        };

        StringHolder holder = parallelExecutor.invoke(StringHolder.class, inv);

        assertEquals(holder.toString(), "FutureThunk[org.apache.tapestry5.ioc.StringHolder]");

        try
        {
            holder.getValue();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Future failure!");
        }
    }

}
