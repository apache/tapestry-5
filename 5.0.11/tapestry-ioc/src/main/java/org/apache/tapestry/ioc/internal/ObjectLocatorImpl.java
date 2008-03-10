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

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectLocator;

/**
 * Base service locator class used when only the module is known (i.e., when instantiating a module
 * builder class).
 */
public class ObjectLocatorImpl implements ObjectLocator
{
    private final InternalRegistry _registry;

    private final Module _module;

    public ObjectLocatorImpl(InternalRegistry registry, Module module)
    {
        _registry = registry;
        _module = module;
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        String expandedServiceId = _registry.expandSymbols(serviceId);

        return _registry.getService(expandedServiceId, serviceInterface);
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        return _registry.getService(serviceInterface);
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return _registry.getObject(objectType, annotationProvider);
    }

    protected InternalRegistry getRegistry()
    {
        return _registry;
    }

    protected Module getModule()
    {
        return _module;
    }

    public <T> T autobuild(Class<T> clazz)
    {
        return _registry.autobuild(clazz);
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return _registry.proxy(interfaceClass, implementationClass);
    }

}
