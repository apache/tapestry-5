// Copyright 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;

import java.lang.reflect.Method;

/**
 * Responsible for creating a {@link ReloadableServiceImplementationObjectCreator} for a service implementation.
 */
@SuppressWarnings("unchecked")
public class ReloadableObjectCreatorSource implements ObjectCreatorSource
{
    private final PlasticProxyFactory proxyFactory;

    private final Method bindMethod;

    private final Class serviceInterfaceClass;

    private final Class serviceImplementationClass;

    private final boolean eagerLoad;

    public ReloadableObjectCreatorSource(PlasticProxyFactory proxyFactory, Method bindMethod,
                                         Class serviceInterfaceClass, Class serviceImplementationClass, boolean eagerLoad)
    {
        this.proxyFactory = proxyFactory;
        this.bindMethod = bindMethod;
        this.serviceInterfaceClass = serviceInterfaceClass;
        this.serviceImplementationClass = serviceImplementationClass;
        this.eagerLoad = eagerLoad;
    }


    @Override
    public ObjectCreator constructCreator(final ServiceBuilderResources resources)
    {
        return new ObjectCreator()
        {
            @Override
            public Object createObject()
            {
                return createReloadableProxy(resources);
            }

            @Override
            public String toString()
            {
                return proxyFactory.getMethodLocation(bindMethod).toString();
            }
        };
    }

    @Override
    public String getDescription()
    {
        return String.format("Reloadable %s via %s", serviceImplementationClass.getName(),
                proxyFactory.getMethodLocation(bindMethod));
    }

    private Object createReloadableProxy(ServiceBuilderResources resources)
    {
        ReloadableServiceImplementationObjectCreator reloadableCreator = new ReloadableServiceImplementationObjectCreator(proxyFactory,
                resources, proxyFactory.getClassLoader(), serviceImplementationClass.getName());

        resources.getService(UpdateListenerHub.class).addUpdateListener(reloadableCreator);

        if (eagerLoad)
        {
            reloadableCreator.createObject();
        }

        return proxyFactory.createProxy(serviceInterfaceClass, resources.getServiceImplementation(), reloadableCreator, getDescription());
    }
}
