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

package org.apache.tapestry5.ioc.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * Serialization support for service proxies.
 */
class SerializationSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationSupport.class);

    // We use a weak reference so that the underlying Registry can be reclaimed by the garbage collector
    // even if it is not explicitly shut down.

    private static WeakReference<ServiceProxyProvider> providerRef;

    static synchronized void setProvider(ServiceProxyProvider proxyProvider)
    {
        ServiceProxyProvider existing = currentProvider();

        if (existing != null) LOGGER.error(IOCMessages.overlappingServiceProxyProviders());

        providerRef = new WeakReference<ServiceProxyProvider>(proxyProvider);
    }

    // Only invoked from synchronized blocks
    private static ServiceProxyProvider currentProvider()
    {
        return providerRef == null ? null : providerRef.get();
    }

    static synchronized void clearProvider(ServiceProxyProvider proxyProvider)
    {
        ServiceProxyProvider existing = currentProvider();

        // The registry does a setProvider() at startup, and we want to make sure that we're the only Registry, that
        // there hasn't been another setProvider() by another Registry.

        if (existing != proxyProvider)
        {
            LOGGER.error(IOCMessages.unexpectedServiceProxyProvider());
            return;
        }

        // Good. It's all the expected simple case, without duelling registries. Kill the reference
        // to the registry.

        providerRef = null;
    }

    static synchronized Object readResolve(String serviceId)
    {
        ServiceProxyProvider provider = currentProvider();

        if (provider == null) throw new RuntimeException(IOCMessages.noProxyProvider(serviceId));

        return provider.provideServiceProxy(serviceId);
    }

    static ServiceProxyToken createToken(String serviceId)
    {
        return new ServiceProxyToken(serviceId);
    }

}
