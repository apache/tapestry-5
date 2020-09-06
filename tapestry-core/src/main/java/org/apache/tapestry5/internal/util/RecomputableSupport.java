// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.ioc.Invokable;

/**
 * A utility class for managing a cacheable result that can be recomputed when needed.
 */
public class RecomputableSupport
{
    private volatile int masterVersion = 1;

    /**
     * Invalidates any existing {@link #create(org.apache.tapestry5.ioc.Invokable)} wrappers} such that they will
     * re-perform the computation when next invoked.
     */
    public void invalidate()
    {
        masterVersion++;
    }

    /**
     * Forces {@link #invalidate()} to be invoked when the hub emits an {@linkplain InvalidationEventHub#addInvalidationCallback(Runnable) invalidation callback}.
     *
     * @param hub
     */
    public void initialize(InvalidationEventHub hub)
    {
        hub.addInvalidationCallback(new Runnable()
        {
            public void run()
            {
                invalidate();
            }
        });
    }

    /**
     * Wraps a computation with caching logic; once computed, the Invokable will return the same value, until
     * {@link #invalidate()} is invoked.
     *
     * @param invokable
     *         a computation to perform, whose results are cacheable until invalidated
     * @param <T>
     *         type of result
     * @return caching-enabled wrapper around invokable
     */
    public <T> Invokable<T> create(final Invokable<T> invokable)
    {
        return new Invokable<T>()
        {
            private volatile int localVersion = masterVersion;

            private volatile T cachedResult;

            public T invoke()
            {
                // Has the master version changed since the computation was last executed?
                if (localVersion != masterVersion)
                {
                    cachedResult = null;
                    localVersion = masterVersion;
                }

                if (cachedResult == null)
                {
                    cachedResult = invokable.invoke();
                }


                return cachedResult;
            }
        };
    }
}
