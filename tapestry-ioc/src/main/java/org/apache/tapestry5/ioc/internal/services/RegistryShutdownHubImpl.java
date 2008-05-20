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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newThreadSafeList;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.slf4j.Logger;

import java.util.List;

public class RegistryShutdownHubImpl implements RegistryShutdownHub
{
    private final OneShotLock lock = new OneShotLock();

    private final Logger logger;

    private final List<RegistryShutdownListener> listeners = newThreadSafeList();

    public RegistryShutdownHubImpl(Logger logger)
    {
        this.logger = logger;
    }

    public void addRegistryShutdownListener(RegistryShutdownListener listener)
    {
        lock.check();

        listeners.add(listener);
    }

    /**
     * Fires the {@link RegistryShutdownListener#registryDidShutdown()} method on each listener. At the end, all the
     * listeners are discarded.
     */
    public void fireRegistryDidShutdown()
    {
        lock.lock();

        for (RegistryShutdownListener l : listeners)
        {
            try
            {
                l.registryDidShutdown();
            }
            catch (Exception ex)
            {
                logger.error(ServiceMessages.shutdownListenerError(l, ex), ex);
            }
        }

        listeners.clear();
    }

}
