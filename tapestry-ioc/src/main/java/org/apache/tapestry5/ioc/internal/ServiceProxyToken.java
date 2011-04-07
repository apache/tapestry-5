// Copyright 2007, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Token that replaces a service proxy when the proxy is serialized.
 */
public class ServiceProxyToken implements Serializable
{
    private static final long serialVersionUID = 4119675138731356650L;

    private final String serviceId;

    public ServiceProxyToken(String serviceId)
    {
        this.serviceId = serviceId;
    }

    Object readResolve() throws ObjectStreamException
    {
        try
        {
            return SerializationSupport.readResolve(serviceId);
        }
        catch (Exception ex)
        {
            ObjectStreamException ose = new InvalidObjectException(ex.getMessage());
            ose.initCause(ex);

            throw ose;
        }
    }

}
