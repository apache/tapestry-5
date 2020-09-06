// Copyright 2006, 2007, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.internal.services.ServiceMessages;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.slf4j.Logger;

import static org.apache.tapestry5.commons.util.CollectionFactory.newThreadSafeList;

import java.util.List;

public class RegistryShutdownHubImpl implements RegistryShutdownHub
{
    private final OneShotLock lock = new OneShotLock();

    private final Logger logger;

    private final List<Runnable> listeners = newThreadSafeList();

    private final List<Runnable> preListeners = newThreadSafeList();

    public RegistryShutdownHubImpl(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void addRegistryShutdownListener(final RegistryShutdownListener listener)
    {
        assert listener != null;

        addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                listener.registryDidShutdown();
            }
        });
    }

    @Override
    public void addRegistryShutdownListener(Runnable listener)
    {
        assert listener != null;

        lock.check();

        listeners.add(listener);
    }

    @Override
    public void addRegistryWillShutdownListener(Runnable listener)
    {
        assert listener != null;

        lock.check();

        preListeners.add(listener);
    }

    /**
     * Fires the {@link RegistryShutdownListener#registryDidShutdown()} method on each listener. At the end, all the
     * listeners are discarded.
     */
    public void fireRegistryDidShutdown()
    {
        lock.lock();

        F.flow(preListeners).concat(listeners).each(new Worker<Runnable>()
        {
            @Override
            public void work(Runnable element)
            {
                try
                {
                    element.run();
                } catch (RuntimeException ex)
                {
                    logger.error(ServiceMessages.shutdownListenerError(element, ex), ex);
                }
            }
        });

        preListeners.clear();
        listeners.clear();
    }

}
