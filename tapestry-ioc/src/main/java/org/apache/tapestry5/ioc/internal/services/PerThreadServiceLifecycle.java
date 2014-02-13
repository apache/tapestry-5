// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;

/**
 * Allows a service to exist "per thread" (in each thread). Creates a proxy that delegates to a per-thread instance.
 * <p/>
 * This scheme ensures that, although the service builder method will be invoked many times over the life of the
 * application, the service decoration process occurs only once. The final calling chain is: Service Proxy --&gt;
 * Interceptor(s) (from Decorators) --&gt; Advise Proxy (from Advisiors) --&gt; PerThread Proxy --&gt; (per thread)
 * instance.
 */
@SuppressWarnings("all")
public class PerThreadServiceLifecycle implements ServiceLifecycle
{
    private final PerthreadManager perthreadManager;

    private final PlasticProxyFactory proxyFactory;

    public PerThreadServiceLifecycle(@Builtin
    PerthreadManager perthreadManager,

    @Builtin
    PlasticProxyFactory proxyFactory)
    {
        this.perthreadManager = perthreadManager;
        this.proxyFactory = proxyFactory;
    }

    /**
     * Returns false; this lifecycle represents a service that will be created many times (by each thread).
     */
    public boolean isSingleton()
    {
        return false;
    }

    public Object createService(ServiceResources resources, ObjectCreator creator)
    {
        ObjectCreator perThreadCreator = new PerThreadServiceCreator(perthreadManager, creator);

        Class serviceInterface = resources.getServiceInterface();

        return proxyFactory.createProxy(serviceInterface, perThreadCreator, String.format("<PerThread Proxy for %s(%s)>", resources.getServiceId(), serviceInterface.getName()));
    }
}
