// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class PerthreadManagerImplTest extends IOCTestCase
{
    @Test
    public void no_listeners()
    {
        Logger logger = mockLogger();

        replay();

        new PerthreadManagerImpl(logger).cleanup();

        verify();
    }

    @Test
    public void listeners_are_one_shot()
    {
        Logger logger = mockLogger();
        ThreadCleanupListener listener = mockThreadCleanupListener();

        listener.threadDidCleanup();

        replay();

        PerthreadManagerImpl hub = new PerthreadManagerImpl(logger);

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

        Logger logger = mockLogger();

        ThreadCleanupListener listener = new ThreadCleanupListener()
        {

            public void threadDidCleanup()
            {
                throw t;
            }

        };

        logger.warn(ServiceMessages.threadCleanupError(listener, t), t);

        replay();

        PerthreadManagerImpl hub = new PerthreadManagerImpl(logger);

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
    // final PerthreadManager hub = new PerthreadManagerImpl(log);
    //
    // hub.addThreadCleanupListener(l1);
    //
    // hub.cleanup();
    //
    // verify();
    // }
}
