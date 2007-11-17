package org.apache.tapestry.ioc.internal;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Token that replaces a service proxy when the proxy is serialized.
 */
class ServiceProxyToken implements Serializable
{
    private final String _serviceId;

    ServiceProxyToken(String serviceId)
    {
        _serviceId = serviceId;
    }

    Object readResolve() throws ObjectStreamException
    {
        try
        {
            return SerializationSupport.readResolve(_serviceId);
        }
        catch (Exception ex)
        {
            ObjectStreamException ose = new InvalidObjectException(ex.getMessage());
            ose.initCause(ex);

            throw ose;
        }
    }

}
