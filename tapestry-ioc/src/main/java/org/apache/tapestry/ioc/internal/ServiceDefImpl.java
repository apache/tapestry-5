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

import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ServiceDef;

import java.util.Set;

public class ServiceDefImpl implements ServiceDef
{
    private final Class serviceInterface;

    private final String serviceId;

    private final String scope;

    private final boolean eagerLoad;

    private final ObjectCreatorSource source;

    private final Set<Class> markers;

    /**
     * @param serviceInterface interface implemented by the service (or the service implementation class, for
     *                         non-proxied services)
     * @param serviceId        unique id for the service
     * @param markers          set of marker annotation classes (will be retained not copied)
     * @param scope            scope of the service (i.e., {@link IOCConstants#DEFAULT_SCOPE}).
     * @param eagerLoad        if true, the service is realized at startup, rather than on-demand
     * @param source           used to create the service implementation when needed
     */
    ServiceDefImpl(Class serviceInterface, String serviceId, Set<Class> markers, String scope,
                   boolean eagerLoad, ObjectCreatorSource source)
    {
        this.serviceInterface = serviceInterface;
        this.serviceId = serviceId;
        this.scope = scope;
        this.eagerLoad = eagerLoad;
        this.source = source;

        this.markers = markers;
    }

    @Override
    public String toString()
    {
        return source.getDescription();
    }

    public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
    {
        return source.constructCreator(resources);
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public Class getServiceInterface()
    {
        return serviceInterface;
    }

    public String getServiceScope()
    {
        return scope;
    }

    public boolean isEagerLoad()
    {
        return eagerLoad;
    }

    public Set<Class> getMarkers()
    {
        return markers;
    }

}
