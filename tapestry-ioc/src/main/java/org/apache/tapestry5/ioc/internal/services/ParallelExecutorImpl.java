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
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.ThunkCreator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ParallelExecutorImpl implements ParallelExecutor
{
    private final ThunkCreator thunkCreator;

    private final ExecutorService executorService;

    private final PerthreadManager perthreadManager;

    public ParallelExecutorImpl(ExecutorService executorService, ThunkCreator thunkCreator,
                                PerthreadManager perthreadManager)
    {
        this.executorService = executorService;
        this.thunkCreator = thunkCreator;
        this.perthreadManager = perthreadManager;
    }

    public <T> Future<T> invoke(Invokable<T> invocable)
    {
        Defense.notNull(invocable, "invocable");

        return executorService.submit(toCallable(invocable));
    }

    private <T> Callable<T> toCallable(final Invokable<T> invocable)
    {
        return new Callable<T>()
        {
            public T call() throws Exception
            {
                try
                {
                    return invocable.invoke();
                }
                finally
                {
                    perthreadManager.cleanup();
                }
            }
        };
    }

    public <T> T invoke(Class<T> proxyType, Invokable<T> invocable)
    {
        final Future<T> future = invoke(invocable);

        ObjectCreator creator = new ObjectCreator()
        {
            public Object createObject()
            {
                try
                {
                    return future.get();
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        };

        String description = String.format("FutureThunk[%s]", proxyType.getName());

        return thunkCreator.createThunk(proxyType, new CachingObjectCreator(creator), description);
    }
}
