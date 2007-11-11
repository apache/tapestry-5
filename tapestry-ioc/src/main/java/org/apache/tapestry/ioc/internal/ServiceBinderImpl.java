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

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;

import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBinder;
import org.apache.tapestry.ioc.ServiceBindingOptions;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.annotations.EagerLoad;
import org.apache.tapestry.ioc.annotations.Marker;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.ClassFactory;

public class ServiceBinderImpl implements ServiceBinder, ServiceBindingOptions
{
    private final OneShotLock _lock = new OneShotLock();

    private final ServiceDefAccumulator _accumulator;

    private final ClassFactory _classFactory;

    private final Set<Class> _defaultMarkers;

    public ServiceBinderImpl(ServiceDefAccumulator accumulator, ClassFactory classFactory,
            Set<Class> defaultMarkers)
    {
        _accumulator = accumulator;
        _classFactory = classFactory;
        _defaultMarkers = defaultMarkers;
    }

    private String _serviceId;

    private Class _serviceInterface;

    private final Set<Class> _markers = CollectionFactory.newSet();

    private Class _serviceImplementation;

    private boolean _eagerLoad;

    private String _scope;

    public void finish()
    {
        _lock.lock();

        flush();
    }

    protected void flush()
    {
        if (_serviceInterface == null) return;

        final Constructor constructor = findConstructor();

        ObjectCreatorSource source = new ObjectCreatorSource()
        {
            public ObjectCreator constructCreator(ServiceBuilderResources resources)
            {
                return new ConstructorServiceCreator(resources, getDescription(), constructor);
            }

            public String getDescription()
            {
                return _classFactory.getConstructorLocation(constructor).toString();
            }
        };

        // Combine service-specific markers with those inherited form the module.
        Set<Class> markers = CollectionFactory.newSet(_defaultMarkers);
        markers.addAll(_markers);

        ServiceDef serviceDef = new ServiceDefImpl(_serviceInterface, _serviceId, markers, _scope,
                _eagerLoad, source);

        _accumulator.addServiceDef(serviceDef);

        _serviceId = null;
        _serviceInterface = null;
        _markers.clear();
        _serviceImplementation = null;
        _eagerLoad = false;
        _scope = null;
    }

    private Constructor findConstructor()
    {
        Constructor result = InternalUtils.findAutobuildConstructor(_serviceImplementation);

        if (result == null)
            throw new RuntimeException(IOCMessages
                    .noConstructor(_serviceImplementation, _serviceId));

        return result;
    }

    public <T> ServiceBindingOptions bind(Class<T> implementationClass)
    {
        return bind(implementationClass, implementationClass);
    }

    public <T> ServiceBindingOptions bind(Class<T> serviceInterface,
            Class<? extends T> serviceImplementation)
    {
        notNull(serviceInterface, "serviceIterface");
        notNull(serviceImplementation, "serviceImplementation");

        _lock.check();

        flush();

        _serviceInterface = serviceInterface;
        _serviceImplementation = serviceImplementation;

        // Set defaults for the other properties.

        _eagerLoad = serviceImplementation.getAnnotation(EagerLoad.class) != null;
        _serviceId = serviceInterface.getSimpleName();

        Scope scope = serviceImplementation.getAnnotation(Scope.class);

        _scope = scope != null ? scope.value() : IOCConstants.DEFAULT_SCOPE;

        Marker marker = serviceImplementation.getAnnotation(Marker.class);

        if (marker != null) _markers.addAll(Arrays.asList(marker.value()));

        return this;
    }

    public ServiceBindingOptions eagerLoad()
    {
        _lock.check();

        _eagerLoad = true;

        return this;
    }

    public ServiceBindingOptions withId(String id)
    {
        notBlank(id, "id");

        _lock.check();

        _serviceId = id;

        return this;
    }

    public ServiceBindingOptions scope(String scope)
    {
        notBlank(scope, "scope");

        _lock.check();

        _scope = scope;

        return this;
    }

    public <T extends Annotation> ServiceBindingOptions withMarker(Class<T>... marker)
    {
        _lock.check();

        _markers.addAll(Arrays.asList(marker));

        return this;
    }

}
