// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.internal.services.AnnotationProviderChain;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

public class ServiceDefImpl implements ServiceDef3
{
    private final Class serviceInterface;

    private final Class serviceImplementation;

    private final String serviceId;

    private final String scope;

    private final boolean eagerLoad;

    private final ObjectCreatorSource source;

    private final Set<Class> markers;

    private final boolean preventDecoration;

    /**
     * @param serviceInterface
     *            interface implemented by the service (or the service implementation class, for
     *            non-proxied services)
     * @param serviceImplementation
     *            service implementation class if exists
     * @param serviceId
     *            unique id for the service
     * @param markers
     *            set of marker annotation classes (will be retained not copied)
     * @param scope
     *            scope of the service (i.e., {@link org.apache.tapestry5.ioc.ScopeConstants#DEFAULT}).
     * @param eagerLoad
     *            if true, the service is realized at startup, rather than on-demand
     * @param preventDecoration
     *            if true, the service may not be decorated
     * @param source
     *            used to create the service implementation when needed
     */
    ServiceDefImpl(Class serviceInterface, Class serviceImplementation, String serviceId, Set<Class> markers,
            String scope, boolean eagerLoad, boolean preventDecoration, ObjectCreatorSource source)
    {
        this.serviceInterface = serviceInterface;
        this.serviceImplementation = serviceImplementation;
        this.serviceId = serviceId;
        this.scope = scope;
        this.eagerLoad = eagerLoad;
        this.preventDecoration = preventDecoration;
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

    public boolean isPreventDecoration()
    {
        return preventDecoration;
    }

    private Flow<Class> searchPath()
    {
        return F.flow(serviceImplementation, serviceInterface).removeNulls();
    }

    public AnnotationProvider getClassAnnotationProvider()
    {
        return AnnotationProviderChain.create(searchPath().map(InternalUtils.CLASS_TO_AP_MAPPER).toList());
    }

    public AnnotationProvider getMethodAnnotationProvider(final String methodName, final Class... argumentTypes)
    {
        return AnnotationProviderChain.create(searchPath().map(new Mapper<Class, Method>()
        {
            public Method map(Class element)
            {
                return InternalUtils.findMethod(element, methodName, argumentTypes);
            }
        }).map(InternalUtils.METHOD_TO_AP_MAPPER).toList());
    }
}
