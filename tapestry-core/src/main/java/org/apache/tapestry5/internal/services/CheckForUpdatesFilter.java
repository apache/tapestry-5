// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.internal.util.ConcurrentBarrier;
import org.apache.tapestry5.services.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Implements a barrier that periodically asks the {@link org.apache.tapestry5.services.UpdateListenerHub} to check for
 * updates to files. The UpdateListenerHub is invoked from a write method, meaning that when it is called, all other
 * threads will be blocked.
 */
public class CheckForUpdatesFilter implements RequestFilter
{
    private final long checkInterval;

    private final long updateTimeout;

    private final UpdateListenerHub updateListenerHub;

    private final ConcurrentBarrier barrier = new ConcurrentBarrier();

    private final Runnable checker = new Runnable()
    {
        public void run()
        {
            // On a race condition, multiple threads may hit this method briefly. If we've
            // already done a check, don't run it again.

            if (System.currentTimeMillis() - lastCheck >= checkInterval)
            {

                // Fire the update event which will force a number of checks and then
                // corresponding invalidation events.

                updateListenerHub.fireCheckForUpdates();

                lastCheck = System.currentTimeMillis();
            }
        }
    };

    private long lastCheck = 0;

    /**
     * @param updateListenerHub invoked, at intervals, to spur the process of detecting changes
     * @param checkInterval     interval, in milliseconds, between checks
     * @param updateTimeout     time, in  milliseconds, to wait to obtain update lock.
     */
    public CheckForUpdatesFilter(UpdateListenerHub updateListenerHub, long checkInterval, long updateTimeout)
    {
        this.updateListenerHub = updateListenerHub;
        this.checkInterval = checkInterval;
        this.updateTimeout = updateTimeout;
    }

    public boolean service(final Request request, final Response response, final RequestHandler handler)
            throws IOException
    {
        final Holder<IOException> exceptionHolder = new Holder<IOException>();

        Invokable<Boolean> invokable = new Invokable<Boolean>()
        {
            public Boolean invoke()
            {
                if (System.currentTimeMillis() - lastCheck >= checkInterval)
                    barrier.tryWithWrite(checker, updateTimeout, TimeUnit.MILLISECONDS);

                // And, now, back to code within the read lock.

                try
                {
                    return handler.service(request, response);
                }
                catch (IOException ex)
                {
                    exceptionHolder.put(ex);
                    return false;
                }
            }
        };

        // Obtain a read lock while handling the request. This will not impair parallel operations, except when a file check
        // is needed (the exclusive write lock will block threads attempting to get a read lock).

        boolean result = barrier.withRead(invokable);

        if (exceptionHolder.hasValue()) throw exceptionHolder.get();

        return result;
    }
}
