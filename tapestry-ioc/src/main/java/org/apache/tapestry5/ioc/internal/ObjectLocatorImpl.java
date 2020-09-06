// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import java.lang.annotation.Annotation;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.ObjectLocator;

/**
 * Base service locator class used when only the module is known (i.e., when instantiating a module
 * class).
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

    @Override
    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        String expandedServiceId = registry.expandSymbols(serviceId);

        return registry.getService(expandedServiceId, serviceInterface);
    }

    @Override
    public <T> T getService(Class<T> serviceInterface)
    {
        return registry.getService(serviceInterface);
    }

    @Override
    public <T> T getService(Class<T> serviceInterface, Class<? extends Annotation>... markerTypes)
    {
        return registry.getService(serviceInterface, markerTypes);
    }

    @Override
    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return registry.getObject(objectType, annotationProvider, this, module);
    }

    @Override
    public <T> T autobuild(Class<T> clazz)
    {
        return registry.autobuild(clazz);
    }

    @Override
    public <T> T autobuild(String description, Class<T> clazz)
    {
        return registry.autobuild(description, clazz);
    }

    @Override
    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return registry.proxy(interfaceClass, implementationClass, this);
    }
}
