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
import org.apache.tapestry5.ioc.services.ParallelExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of {@link ParallelExecutor} used when {@linkplain org.apache.tapestry5.IOCSymbols#THREAD_POOL_ENABLED
 * the thread pool is disabled}.
 *
 * @since 5.1.0.3
 */
public class NonParallelExecutor implements ParallelExecutor
{
    public <T> Future<T> invoke(Invokable<T> invocable)
    {
        final T result = invocable.invoke();

        return new Future<T>()
        {
            public boolean cancel(boolean mayInterruptIfRunning)
            {
                return false;
            }

            public boolean isCancelled()
            {
                return false;
            }

            public boolean isDone()
            {
                return true;
            }

            public T get() throws InterruptedException, ExecutionException
            {
                return result;
            }

            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
            {
                return result;
            }
        };
    }

    public <T> T invoke(Class<T> proxyType, Invokable<T> invocable)
    {
        return invocable.invoke();
    }
}
