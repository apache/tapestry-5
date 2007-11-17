package org.apache.tapestry.ioc.internal;

/**
 * Used in concert with {@link org.apache.tapestry.ioc.internal.SerializationSupport} to
 * convert service tokens back into service proxies.
 */
public interface ServiceProxyProvider
{
    /**
     * Look up the service and return it's proxy.
     *
     * @param serviceId the id of the service to obtain
     * @return the service proxy
     * @throws RuntimeException if the service does not exist or does not have a proxy
     */
    Object provideServiceProxy(String serviceId);
}
