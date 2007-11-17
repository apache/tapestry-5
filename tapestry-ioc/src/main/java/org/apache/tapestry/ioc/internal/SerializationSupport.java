package org.apache.tapestry.ioc.internal;

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

    private static WeakReference<ServiceProxyProvider> _providerRef;

    static synchronized void setProvider(ServiceProxyProvider proxyProvider)
    {
        ServiceProxyProvider existing = currentProvider();

        if (existing != null) LOGGER.error(IOCMessages.overlappingServiceProxyProviders());

        _providerRef = new WeakReference<ServiceProxyProvider>(proxyProvider);
    }

    private static ServiceProxyProvider currentProvider()
    {
        return _providerRef == null ? null : _providerRef.get();
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

        _providerRef = null;
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