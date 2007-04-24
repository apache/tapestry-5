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

package org.apache.tapestry.ioc.internal;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.def.ServiceDef;

/**
 * Decorator for {@link org.apache.tapestry.ioc.ObjectCreator} that ensures the service is only
 * created once. This detects a situation where the service builder for a service directly or
 * indirectly invokes methods on the service itself. This would show up as a second call up the
 * ServiceCreator stack injected into the proxy.
 */
public class OneShotServiceCreator implements ObjectCreator
{
    private final ServiceDef _serviceDef;

    private final ObjectCreator _delegate;

    private final Log _log;

    private boolean _locked;

    public OneShotServiceCreator(ServiceDef serviceDef, ObjectCreator delegate, Log log)
    {
        _serviceDef = serviceDef;
        _delegate = delegate;
        _log = log;
    }

    /**
     * We could make this method synchronized, but in the context of creating a service for a proxy,
     * it will already be synchronized (inside the proxy).
     */
    public Object createObject()
    {
        if (_locked)
            throw new IllegalStateException(IOCMessages.recursiveServiceBuild(_serviceDef));

        // Set the lock, to ensure that recursive service construction fails.

        _locked = true;

        try
        {
            return _delegate.createObject();
        }
        catch (RuntimeException ex)
        {
            _log.error(IOCMessages.serviceConstructionFailed(_serviceDef, ex), ex);

            // Release the lock on failure; the service is now in an unknown state, but we may
            // be able to continue from here.

            _locked = false;

            throw ex;
        }

    }
}
