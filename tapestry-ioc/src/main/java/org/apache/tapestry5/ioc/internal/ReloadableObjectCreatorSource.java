// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.services.UpdateListenerHub;

/**
 * Responsible for creating a {@link ReloadableObjectCreator} for a service implementation.
 */
public class ReloadableObjectCreatorSource implements ObjectCreatorSource
{
    private final ClassFactory classFactory;

    private final Method bindMethod;

    private final Class serviceInterfaceClass;

    private final Class serviceImplementationClass;

    public ReloadableObjectCreatorSource(ClassFactory classFactory, Method bindMethod, Class serviceInterfaceClass,
            Class serviceImplementationClass)
    {
        this.classFactory = classFactory;
        this.bindMethod = bindMethod;
        this.serviceInterfaceClass = serviceInterfaceClass;
        this.serviceImplementationClass = serviceImplementationClass;
    }

    public ObjectCreator constructCreator(final ServiceBuilderResources resources)
    {
        return new ObjectCreator()
        {
            public Object createObject()
            {
                return createReloadableProxy(resources);
            }
        };
    }

    public String getDescription()
    {
        return String.format("Reloadable %s via %s", serviceImplementationClass.getName(), classFactory
                .getMethodLocation(bindMethod));
    }

    private Object createReloadableProxy(ServiceBuilderResources resources)
    {
        ReloadableObjectCreator reloadableCreator = new ReloadableObjectCreator(resources, classFactory
                .getClassLoader(), serviceImplementationClass.getName(), serviceImplementationClass
                .getProtectionDomain());

        resources.getService(UpdateListenerHub.class).addUpdateListener(reloadableCreator);

        return classFactory.createProxy(serviceInterfaceClass, reloadableCreator, getDescription());
    }
}
