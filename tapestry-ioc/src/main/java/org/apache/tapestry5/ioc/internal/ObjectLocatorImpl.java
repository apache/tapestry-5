// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;

/**
 * Base service locator class used when only the module is known (i.e., when instantiating a module class).
 */
public class ObjectLocatorImpl implements ObjectLocator
{
    private final InternalRegistry registry;

    private final Module module;

    public ObjectLocatorImpl(InternalRegistry registry, Module module)
    {
        this.registry = registry;
        this.module = module;
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        String expandedServiceId = registry.expandSymbols(serviceId);

        return registry.getService(expandedServiceId, serviceInterface);
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        return registry.getService(serviceInterface);
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return registry.getObject(objectType, annotationProvider, this, module);
    }

    public <T> T autobuild(Class<T> clazz)
    {
        return registry.autobuild(clazz);
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return registry.proxy(interfaceClass, implementationClass);
    }
}
