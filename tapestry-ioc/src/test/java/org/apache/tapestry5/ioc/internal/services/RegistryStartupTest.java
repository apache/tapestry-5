// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.List;

public class RegistryStartupTest extends IOCTestCase
{
    /**
     * Runnable runs.
     */
    @Test
    public void success()
    {
        List<Runnable> configuration = CollectionFactory.newList();

        Runnable r1 = newMock(Runnable.class);
        Runnable r2 = newMock(Runnable.class);

        configuration.add(r1);
        configuration.add(r2);

        Logger logger = mockLogger();

        getMocksControl().checkOrder(true);

        r1.run();
        r2.run();

        replay();

        Runnable startup = new RegistryStartup(logger, configuration);

        startup.run();

        verify();

        // The configuration is cleared out at the end of the execution.
        assertTrue(configuration.isEmpty());
    }

    @Test
    public void failure_is_logged_but_execution_continues()
    {
        List<Runnable> configuration = CollectionFactory.newList();
        RuntimeException t = new RuntimeException("Runnable r1 has been a naughty boy.");

        Runnable r1 = newMock(Runnable.class);
        Runnable r2 = newMock(Runnable.class);

        configuration.add(r1);
        configuration.add(r2);

        Logger logger = mockLogger();

        getMocksControl().checkOrder(true);

        r1.run();
        setThrowable(t);

        logger.error(ServiceMessages.startupFailure(t));

        r2.run();

        replay();

        Runnable startup = new RegistryStartup(logger, configuration);

        startup.run();

        verify();
    }

    @Test
    public void run_may_only_be_called_once()
    {
        Logger logger = mockLogger();
        List<Runnable> configuration = CollectionFactory.newList();

        replay();

        Runnable startup = new RegistryStartup(logger, configuration);

        startup.run();

        try
        {
            startup.run();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertMessageContains(ex, "Method org.apache.tapestry5.ioc.internal.services.RegistryStartup.run(",
                                  "may no longer be invoked.");

        }

        verify();
    }

    @Test
    public void integration()
    {
        Registry r = buildRegistry(StartupModule.class);

        assertFalse(StartupModule.startupInvoked);

        r.performRegistryStartup();

        assertTrue(StartupModule.startupInvoked);

        // Ideally we'd have a way to show that the PerthreadManager was notified after
        // RegistryStartup did its thing, but ...

        r.shutdown();
    }
}
