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

import org.apache.tapestry.ioc.*;
import org.apache.tapestry.ioc.annotation.EagerLoad;
import org.apache.tapestry.ioc.annotation.Marker;
import org.apache.tapestry.ioc.annotation.Scope;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;

public class ServiceBinderImpl implements ServiceBinder, ServiceBindingOptions
{
    private final OneShotLock lock = new OneShotLock();

    private final ServiceDefAccumulator accumulator;

    private final ClassFactory classFactory;

    private final Set<Class> defaultMarkers;

    public ServiceBinderImpl(ServiceDefAccumulator accumulator, ClassFactory classFactory, Set<Class> defaultMarkers)
    {
        this.accumulator = accumulator;
        this.classFactory = classFactory;
        this.defaultMarkers = defaultMarkers;
    }

    private String serviceId;

    private Class serviceInterface;

    private final Set<Class> markers = CollectionFactory.newSet();

    private Class serviceImplementation;

    private boolean eagerLoad;

    private String scope;

    public void finish()
    {
        lock.lock();

        flush();
    }

    protected void flush()
    {
        if (serviceInterface == null) return;

        final Constructor constructor = findConstructor();

        ObjectCreatorSource source = new ObjectCreatorSource()
        {
            public ObjectCreator constructCreator(ServiceBuilderResources resources)
            {
                return new ConstructorServiceCreator(resources, getDescription(), constructor);
            }

            public String getDescription()
            {
                return classFactory.getConstructorLocation(constructor).toString();
            }
        };

        // Combine service-specific markers with those inherited form the module.
        Set<Class> markers = CollectionFactory.newSet(defaultMarkers);
        markers.addAll(this.markers);

        ServiceDef serviceDef = new ServiceDefImpl(serviceInterface, serviceId, markers, scope, eagerLoad, source);

        accumulator.addServiceDef(serviceDef);

        serviceId = null;
        serviceInterface = null;
        this.markers.clear();
        serviceImplementation = null;
        eagerLoad = false;
        scope = null;
    }

    private Constructor findConstructor()
    {
        Constructor result = InternalUtils.findAutobuildConstructor(serviceImplementation);

        if (result == null) throw new RuntimeException(IOCMessages
                .noConstructor(serviceImplementation, serviceId));

        return result;
    }

    public <T> ServiceBindingOptions bind(Class<T> implementationClass)
    {
        return bind(implementationClass, implementationClass);
    }

    public <T> ServiceBindingOptions bind(Class<T> serviceInterface, Class<? extends T> serviceImplementation)
    {
        notNull(serviceInterface, "serviceIterface");
        notNull(serviceImplementation, "serviceImplementation");

        lock.check();

        flush();

        this.serviceInterface = serviceInterface;
        this.serviceImplementation = serviceImplementation;

        // Set defaults for the other properties.

        eagerLoad = serviceImplementation.getAnnotation(EagerLoad.class) != null;
        serviceId = serviceInterface.getSimpleName();

        Scope scope = serviceImplementation.getAnnotation(Scope.class);

        this.scope = scope != null ? scope.value() : IOCConstants.DEFAULT_SCOPE;

        Marker marker = serviceImplementation.getAnnotation(Marker.class);

        if (marker != null)
        {
            InternalUtils.validateMarkerAnnotations(marker.value());
            markers.addAll(Arrays.asList(marker.value()));
        }

        return this;
    }

    public ServiceBindingOptions eagerLoad()
    {
        lock.check();

        eagerLoad = true;

        return this;
    }

    public ServiceBindingOptions withId(String id)
    {
        notBlank(id, "id");

        lock.check();

        serviceId = id;

        return this;
    }

    public ServiceBindingOptions scope(String scope)
    {
        notBlank(scope, "scope");

        lock.check();

        this.scope = scope;

        return this;
    }

    public <T extends Annotation> ServiceBindingOptions withMarker(Class<T>... marker)
    {
        lock.check();

        InternalUtils.validateMarkerAnnotations(marker);

        markers.addAll(Arrays.asList(marker));

        return this;
    }

}
