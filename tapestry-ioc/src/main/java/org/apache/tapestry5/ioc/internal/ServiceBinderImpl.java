// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.ServiceId;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class ServiceBinderImpl implements ServiceBinder, ServiceBindingOptions
{
    private final OneShotLock lock = new OneShotLock();

    private final Method bindMethod;

    private final ServiceDefAccumulator accumulator;

    private final ClassFactory classFactory;

    private final Set<Class> defaultMarkers;

    private final boolean moduleDefaultPreventDecoration;

    public ServiceBinderImpl(ServiceDefAccumulator accumulator, Method bindMethod,
                             ClassFactory classFactory,
                             Set<Class> defaultMarkers, boolean moduleDefaultPreventDecoration)
    {
        this.accumulator = accumulator;
        this.bindMethod = bindMethod;
        this.classFactory = classFactory;
        this.defaultMarkers = defaultMarkers;
        this.moduleDefaultPreventDecoration = moduleDefaultPreventDecoration;

        clear();
    }

    private String serviceId;

    private Class serviceInterface;

    private Class serviceImplementation;

    private final Set<Class> markers = CollectionFactory.newSet();

    private ObjectCreatorSource source;

    private boolean eagerLoad;

    private String scope;

    private boolean preventDecoration;

    public void finish()
    {
        lock.lock();

        flush();
    }

    protected void flush()
    {
        if (serviceInterface == null) return;

        // source will be null when the implementation class is provided; non-null when using
        // a ServiceBuilder callback

        if (source == null)
            source = createObjectCreatorSourceFromImplementationClass();

        // Combine service-specific markers with those inherited form the module.
        Set<Class> markers = CollectionFactory.newSet(defaultMarkers);
        markers.addAll(this.markers);

        ServiceDef serviceDef = new ServiceDefImpl(serviceInterface, serviceId, markers, scope, eagerLoad,
                                                   preventDecoration, source);

        accumulator.addServiceDef(serviceDef);

        clear();
    }

    private void clear()
    {
        serviceId = null;
        serviceInterface = null;
        serviceImplementation = null;
        source = null;
        this.markers.clear();
        eagerLoad = false;
        scope = null;
        preventDecoration = moduleDefaultPreventDecoration;
    }

    private ObjectCreatorSource createObjectCreatorSourceFromImplementationClass()
    {
        final Constructor constructor = InternalUtils.findAutobuildConstructor(serviceImplementation);

        if (constructor == null)
            throw new RuntimeException(IOCMessages.noConstructor(serviceImplementation, serviceId));

        return new ObjectCreatorSource()
        {
            public ObjectCreator constructCreator(ServiceBuilderResources resources)
            {
                return new ConstructorServiceCreator(resources, getDescription(), constructor);
            }

            public String getDescription()
            {
                return String.format("%s via %s",
                                     classFactory.getConstructorLocation(constructor),
                                     classFactory.getMethodLocation(bindMethod));
            }
        };
    }

    public <T> ServiceBindingOptions bind(Class<T> serviceClass)
    {
        if (serviceClass.isInterface())
        {
            try
            {
                Class<T> implementationClass = (Class<T>) Class.forName(serviceClass.getName() + "Impl");

                if (!implementationClass.isInterface() && serviceClass.isAssignableFrom(implementationClass))
                {
                    return bind(serviceClass, implementationClass);
                }
                throw new RuntimeException(IOCMessages.noServiceMatchesType(serviceClass));
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(IOCMessages.noConventionServiceImplementationFound(serviceClass));
            }
        }

        return bind(serviceClass, serviceClass);
    }

    public <T> ServiceBindingOptions bind(Class<T> serviceInterface, final ServiceBuilder<T> builder)
    {
        Defense.notNull(serviceInterface, "serviceInterface");
        Defense.notNull(builder, "builder");

        lock.check();

        flush();

        this.serviceInterface = serviceInterface;
        this.scope = ScopeConstants.DEFAULT;

        serviceId = serviceInterface.getSimpleName();

        this.source = new ObjectCreatorSource()
        {
            public ObjectCreator constructCreator(final ServiceBuilderResources resources)
            {
                return new ObjectCreator()
                {
                    public Object createObject()
                    {
                        return builder.buildService(resources);
                    }
                };
            }

            public String getDescription()
            {
                return classFactory.getMethodLocation(bindMethod).toString();
            }
        };

        return this;
    }

    public <T> ServiceBindingOptions bind(Class<T> serviceInterface, Class<? extends T> serviceImplementation)
    {
        Defense.notNull(serviceInterface, "serviceIterface");
        Defense.notNull(serviceImplementation, "serviceImplementation");

        lock.check();

        flush();

        this.serviceInterface = serviceInterface;

        this.serviceImplementation = serviceImplementation;

        // Set defaults for the other properties.
        
        
        eagerLoad = serviceImplementation.getAnnotation(EagerLoad.class) != null;
        
        ServiceId serviceIdAnnotation = serviceImplementation.getAnnotation(ServiceId.class);
        
        if(serviceIdAnnotation != null)
        {
            serviceId = serviceIdAnnotation.value();
        }
        else
        {
            serviceId = serviceInterface.getSimpleName();
        }

        Scope scope = serviceImplementation.getAnnotation(Scope.class);

        this.scope = scope != null ? scope.value() : ScopeConstants.DEFAULT;

        Marker marker = serviceImplementation.getAnnotation(Marker.class);

        if (marker != null)
        {
            InternalUtils.validateMarkerAnnotations(marker.value());
            markers.addAll(Arrays.asList(marker.value()));
        }

        preventDecoration |= serviceImplementation.getAnnotation(PreventServiceDecoration.class) != null;

        return this;
    }

    public ServiceBindingOptions eagerLoad()
    {
        lock.check();

        eagerLoad = true;

        return this;
    }

    public ServiceBindingOptions preventDecoration()
    {
        lock.check();

        preventDecoration = true;

        return this;
    }

    public ServiceBindingOptions withId(String id)
    {
        Defense.notBlank(id, "id");

        lock.check();

        serviceId = id;

        return this;
    }

    public ServiceBindingOptions scope(String scope)
    {
        Defense.notBlank(scope, "scope");

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
