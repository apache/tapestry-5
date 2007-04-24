// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.def.ServiceDef;

/**
 * Decorator for {@link org.apache.tapestry.ioc.ObjectCreator} that ensures the service is only
 * created once. This detects a situation where the service builder for a service directly or
 * indirectly invokes methods on the service itself. This would show up as a second call up the
 * ServiceCreator stack injected into the proxy.
 * <p>
 * We could use the {@link org.apache.tapestry.internal.annotations.OneShot} annotation, but this
 * implementation gives us a bit more flexibility to report the error.
 * 
 * 
 */
public class OneShotServiceCreator implements ObjectCreator
{
    private final ServiceDef _serviceDef;

    private final ObjectCreator _delegate;

    private boolean _locked;

    public OneShotServiceCreator(ServiceDef serviceDef, ObjectCreator delegate)
    {
        _serviceDef = serviceDef;
        _delegate = delegate;
    }

    /**
     * We could make this method synchronized, but in the context of creating a service for a proxy,
     * it will already be synchronized (inside the proxy).
     */
    public Object createObject()
    {
        if (_locked)
            throw new IllegalStateException(IOCMessages.recursiveServiceBuild(_serviceDef));

        _locked = true;

        return _delegate.createObject();
    }

}
