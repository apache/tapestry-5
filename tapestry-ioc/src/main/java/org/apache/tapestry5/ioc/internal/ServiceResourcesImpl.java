// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.slf4j.Logger;

/**
 * Implementation of {@link org.apache.tapestry5.ioc.ServiceBuilderResources}. We just have one
 * implementation that fills the purposes of methods that need a {@link org.apache.tapestry5.ioc.ServiceResources}
 * (which includes service decorator methods) as well as methods that need a
 * {@link org.apache.tapestry5.ioc.ServiceBuilderResources} (which is just service builder methods). Since it is most
 * commonly used for the former, we'll just leave the name as ServiceResourcesImpl.
 */
@SuppressWarnings("all")
public class ServiceResourcesImpl extends ObjectLocatorImpl implements ServiceBuilderResources
{
    private final InternalRegistry registry;

    private final Module module;

    private final ServiceDef3 serviceDef;

    private final Logger logger;

    private final PlasticProxyFactory proxyFactory;

    public ServiceResourcesImpl(InternalRegistry registry, Module module, ServiceDef3 serviceDef,
            PlasticProxyFactory proxyFactory, Logger logger)
    {
        super(registry, module);

        this.registry = registry;
        this.module = module;
        this.serviceDef = serviceDef;
        this.proxyFactory = proxyFactory;
        this.logger = logger;
    }

    @Override
    public String getServiceId()
    {
        return serviceDef.getServiceId();
    }

    @Override
    public Class getServiceInterface()
    {
        return serviceDef.getServiceInterface();
    }

    @Override
    public Class getServiceImplementation()
    {
        return serviceDef.getServiceImplementation();
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public <T> Collection<T> getUnorderedConfiguration(final Class<T> valueType)
    {
        Collection<T> result = registry.invoke(
                "Collecting unordered configuration for service " + serviceDef.getServiceId(),
                new Invokable<Collection<T>>()
                {
                    @Override
                    public Collection<T> invoke()
                    {
                        return registry.getUnorderedConfiguration(serviceDef, valueType);
                    }
                });

        logConfiguration(result);

        return result;
    }

    private void logConfiguration(Collection configuration)
    {
        if (logger.isDebugEnabled())
            logger.debug(IOCMessages.constructedConfiguration(configuration));
    }

    @Override
    public <T> List<T> getOrderedConfiguration(final Class<T> valueType)
    {
        List<T> result = registry.invoke("Collecting ordered configuration for service " + serviceDef.getServiceId(),
                new Invokable<List<T>>()
                {
                    @Override
                    public List<T> invoke()
                    {
                        return registry.getOrderedConfiguration(serviceDef, valueType);
                    }
                });

        logConfiguration(result);

        return result;
    }

    @Override
    public <K, V> Map<K, V> getMappedConfiguration(final Class<K> keyType, final Class<V> valueType)
    {
        Map<K, V> result = registry.invoke("Collecting mapped configuration for service " + serviceDef.getServiceId(),
                new Invokable<Map<K, V>>()
                {
                    @Override
                    public Map<K, V> invoke()
                    {
                        return registry.getMappedConfiguration(serviceDef, keyType, valueType);
                    }
                });

        if (logger.isDebugEnabled())
            logger.debug(IOCMessages.constructedConfiguration(result));

        return result;
    }

    @Override
    public Object getModuleBuilder()
    {
        return module.getModuleBuilder();
    }

    @Override
    public <T> T autobuild(String description, final Class<T> clazz)
    {
        assert clazz != null;

        return registry.invoke(description, new Invokable<T>()
        {
            @Override
            public T invoke()
            {
                Constructor constructor = InternalUtils.findAutobuildConstructor(clazz);

                if (constructor == null)
                    throw new RuntimeException(IOCMessages.noAutobuildConstructor(clazz));

                String description = proxyFactory.getConstructorLocation(constructor).toString();

                ObjectCreator creator = new ConstructorServiceCreator(ServiceResourcesImpl.this, description,
                        constructor);

                return clazz.cast(creator.createObject());
            }
        });
    }

    @Override
    public <T> T autobuild(final Class<T> clazz)
    {
        assert clazz != null;

        return autobuild("Autobuilding instance of class " + clazz.getName(), clazz);
    }

    @Override
    public OperationTracker getTracker()
    {
        return registry;
    }

    public Class getImplementationClass()
    {
        return null;
    }

    @Override
    public AnnotationProvider getClassAnnotationProvider()
    {
        return serviceDef.getClassAnnotationProvider();
    }

    @Override
    public AnnotationProvider getMethodAnnotationProvider(String methodName, Class... parameterTypes)
    {
        return serviceDef.getMethodAnnotationProvider(methodName, parameterTypes);
    }
}
