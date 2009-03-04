// Copyright 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.EagerLoadServiceProxy;
import org.apache.tapestry5.ioc.internal.ServiceActivityTracker;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.apache.tapestry5.ioc.services.Status;

/**
 * Invoked from a fabricated service delegate to get or realize (instantiate and configure) the service implementation.
 * This includes synchronization logic, to prevent multiple threads from attempting to realize the same service at the
 * same time (a service should be realized only once). The additional interfaces implemented by this class support eager
 * loading of services (at application startup), and orderly shutdown of proxies.
 */
public class JustInTimeObjectCreator implements ObjectCreator, EagerLoadServiceProxy,
        RegistryShutdownListener
{
    private final ServiceActivityTracker tracker;

    private volatile ObjectCreator creator;

    private volatile Object object;

    private final String serviceId;

    public JustInTimeObjectCreator(ServiceActivityTracker tracker, ObjectCreator creator,
                                   String serviceId)
    {
        this.tracker = tracker;
        this.creator = creator;
        this.serviceId = serviceId;
    }

    /**
     * Checks to see if the proxy has been shutdown, then invokes {@link ObjectCreator#createObject()} if it has not
     * already done so.
     *
     * @throws IllegalStateException if the registry has been shutdown
     */
    public Object createObject()
    {
        if (object == null)
            obtainObjectFromCreator();

        return object;
    }

    private synchronized void obtainObjectFromCreator()
    {
        if (object != null) return;

        try
        {
            object = creator.createObject();

            // And if that's successful ...

            tracker.setStatus(serviceId, Status.REAL);

            creator = null;
        }
        catch (RuntimeException ex)
        {
            throw new RuntimeException(ServiceMessages.serviceBuildFailure(serviceId, ex), ex);
        }
    }

    /**
     * Invokes {@link #createObject()} to force the creation of the underlying service.
     */
    public void eagerLoadService()
    {
        // Force object creation now

        createObject();
    }

    /**
     * Sets the shutdown flag and releases the object and the creator.
     */
    public void registryDidShutdown()
    {
        creator = new ObjectCreator()
        {
            public Object createObject()
            {
                throw new IllegalStateException(ServiceMessages.registryShutdown(serviceId));
            }
        };

        object = null;
    }

}
