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

package org.apache.tapestry.ioc.internal.services;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class ThreadCleanupHubImplTest extends IOCTestCase
{
    @Test
    public void no_listeners()
    {
        Log log = mockLog();

        replay();

        new ThreadCleanupHubImpl(log).cleanup();

        verify();
    }

    @Test
    public void listeners_are_one_shot()
    {
        Log log = mockLog();
        ThreadCleanupListener listener = mockThreadCleanupListener();

        listener.threadDidCleanup();

        replay();

        ThreadCleanupHubImpl hub = new ThreadCleanupHubImpl(log);

        hub.addThreadCleanupListener(listener);

        hub.cleanup();

        verify();

        // No more training.

        replay();

        // Listener not invoked.

        hub.cleanup();

        verify();
    }

    private ThreadCleanupListener mockThreadCleanupListener()
    {
        return newMock(ThreadCleanupListener.class);
    }

    @Test
    public void listener_cleanup_failure()
    {
        final RuntimeException t = new RuntimeException("Boom!");

        Log log = mockLog();

        ThreadCleanupListener listener = new ThreadCleanupListener()
        {

            public void threadDidCleanup()
            {
                throw t;
            }

        };

        log.warn(ServiceMessages.threadCleanupError(listener, t), t);

        replay();

        ThreadCleanupHubImpl hub = new ThreadCleanupHubImpl(log);

        hub.addThreadCleanupListener(listener);

        hub.cleanup();

        verify();
    }

    // @Test
    // public void listener_list_is_per_thread()
    // {
    // ThreadCleanupListener l1 = newThreadCleanupListener();
    // final ThreadCleanupListener l2 = newThreadCleanupListener();
    //
    // Thread thread = new Thread();
    //
    // l1.threadDidCleanup();
    //
    // replay();
    //
    // final ThreadCleanupHub hub = new ThreadCleanupHubImpl(log);
    //
    // hub.addThreadCleanupListener(l1);
    //
    // hub.cleanup();
    //
    // verify();
    // }
}
