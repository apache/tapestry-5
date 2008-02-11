// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.internal.EagerLoadServiceProxy;
import org.apache.tapestry.ioc.internal.ServiceActivityTracker;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.apache.tapestry.ioc.services.Status;

/**
 * Invoked from a fabricated service delegate to get or realize (instantiate and configure) the
 * service implementation. This includes synchronization logic, to prevent multiple threads from
 * attempting to realize the same service at the same time (a service should be realized only once).
 * The additional interfaces implemented by this class support eager loading of services (at
 * application startup), and orderly shutdown of proxies.
 */
public class JustInTimeObjectCreator implements ObjectCreator, EagerLoadServiceProxy,
                                                RegistryShutdownListener
{
    private final ServiceActivityTracker _tracker;

    private ObjectCreator _creator;

    private boolean _shutdown;

    private Object _object;

    private final String _serviceId;

    public JustInTimeObjectCreator(ServiceActivityTracker tracker, ObjectCreator creator,
                                   String serviceId)
    {
        _tracker = tracker;
        _creator = creator;
        _serviceId = serviceId;
    }

    /**
     * Checks to see if the proxy has been shutdown, then invokes
     * {@link ObjectCreator#createObject()} if it has not already done so.
     *
     * @throws IllegalStateException if the registry has been shutdown
     */
    public synchronized Object createObject()
    {
        if (_shutdown)
            throw new IllegalStateException(ServiceMessages.registryShutdown(_serviceId));

        if (_object == null)
        {
            try
            {
                _object = _creator.createObject();

                // And if that's successful ...

                _tracker.setStatus(_serviceId, Status.REAL);

                _creator = null;
            }
            catch (RuntimeException ex)
            {
                throw new RuntimeException(ServiceMessages.serviceBuildFailure(_serviceId, ex), ex);
            }
        }

        return _object;
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
    public synchronized void registryDidShutdown()
    {
        _shutdown = true;
        _object = null;
        _creator = null;
    }

}
