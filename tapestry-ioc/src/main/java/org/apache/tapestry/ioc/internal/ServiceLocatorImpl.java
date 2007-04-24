// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.IOCUtilities.toQualifiedId;

import org.apache.tapestry.ioc.ServiceLocator;

/**
 * Base service locator class used when only the module is known (i.e., when instantiating a module
 * builder class).
 */
public class ServiceLocatorImpl extends Object implements ServiceLocator
{
    private final InternalRegistry _registry;

    private final Module _module;

    public ServiceLocatorImpl(InternalRegistry registry, Module module)
    {
        _registry = registry;
        _module = module;
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        String expandedServiceId = _registry.expandSymbols(serviceId);

        return _registry.getService(
                toQualifiedId(_module.getModuleId(), expandedServiceId),
                serviceInterface,
                _module);
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        return _registry.getService(serviceInterface, _module);
    }

    public <T> T getObject(String reference, Class<T> objectType)
    {
        return _registry.getObject(reference, objectType, this);
    }

    protected InternalRegistry getRegistry()
    {
        return _registry;
    }

    protected Module getModule()
    {
        return _module;
    }
}
