// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.ioc.Registry;

/**
 * A wrapper around {@link InternalRegistry} that exists to expand symbols in a service id before
 * invoking {@link ObjectLocator#getService(Class)}.
 */
public class RegistryWrapper implements Registry
{
    private final InternalRegistry _registry;

    public RegistryWrapper(final InternalRegistry registry)
    {
        _registry = registry;
    }

    public void cleanupThread()
    {
        _registry.cleanupThread();
    }

    public void shutdown()
    {
        _registry.shutdown();
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return _registry.getObject(objectType, annotationProvider);
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

    public <T> T autobuild(Class<T> clazz)
    {
        return _registry.autobuild(clazz);
    }

    public void performRegistryStartup()
    {
        _registry.performRegistryStartup();
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return _registry.proxy(interfaceClass, implementationClass);
    }

}