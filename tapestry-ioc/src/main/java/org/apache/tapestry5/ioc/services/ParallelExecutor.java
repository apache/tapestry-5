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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.Invokable;

import java.util.concurrent.Future;

/**
 * A service that allows work to occur in parallel using a thread pool. The thread pool is started lazily, and is
 * shutdown when the Registry is shutdown.
 *
 * @see org.apache.tapestry5.IOCSymbols
 * @since 5.1.0.1
 */
public interface ParallelExecutor
{
    /**
     * Submits the invocable object to be executed in a pooled thread. Returns a Future object representing the eventual
     * result of the invocable's operation.  The actual operation will be wrapped such that {@link
     * PerthreadManager#cleanup()} is invoked after the operation completes.
     *
     * @param invocable to execute in a thread
     * @param <T>
     * @return Future result of that invocation
     */
    <T> Future<T> invoke(Invokable<T> invocable);

    /**
     * As with {@link #invoke(org.apache.tapestry5.ioc.Invokable)}, but the result is wrapped inside a {@linkplain
     * org.apache.tapestry5.ioc.services.ThunkCreator thunk}. Invoking methods on the thunk will block until the value
     * is available.
     *
     * @param proxyType return type, used to create the thunk
     * @param invocable object that will eventually execute and return a value
     * @param <T>
     * @return the thunk
     */
    <T> T invoke(Class<T> proxyType, Invokable<T> invocable);
}
