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
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.same;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class RegistryShutdownHubImplTest extends IOCInternalTestCase
{

    @Test
    public void add_and_notify()
    {
        RegistryShutdownListener l1 = mockListener();
        RegistryShutdownListener l2 = mockListener();
        Logger logger = mockLogger();

        l1.registryDidShutdown();
        l2.registryDidShutdown();

        replay();

        RegistryShutdownHubImpl hub = new RegistryShutdownHubImpl(logger);

        hub.addRegistryShutdownListener(l1);
        hub.addRegistryShutdownListener(l2);

        hub.fireRegistryDidShutdown();

        verify();
    }

    /**
     * Shows that multiple listener will be notified, and that an error in one doesn't prevent others from being
     * notified.
     */
    @Test
    public void notification_error()
    {
        RegistryShutdownListener l1 = mockListener();
        RegistryShutdownListener l2 = mockListener();
        RegistryShutdownListener l3 = mockListener();

        Logger logger = mockLogger();

        Throwable t = new RuntimeException("Shutdown failure.");

        l1.registryDidShutdown();
        l2.registryDidShutdown();
        setThrowable(t);

        logger.error(contains("Shutdown failure."), same(t));

        l3.registryDidShutdown();

        replay();

        RegistryShutdownHubImpl hub = new RegistryShutdownHubImpl(logger);

        hub.addRegistryShutdownListener(l1);
        hub.addRegistryShutdownListener(l2);
        hub.addRegistryShutdownListener(l3);

        hub.fireRegistryDidShutdown();

        verify();
    }

    private RegistryShutdownListener mockListener()
    {
        return newMock(RegistryShutdownListener.class);
    }
}
