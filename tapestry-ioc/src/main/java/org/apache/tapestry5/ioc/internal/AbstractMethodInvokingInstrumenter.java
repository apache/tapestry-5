// Copyright 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Based class for service decorators and service advisors that work by invoking a module method.
 *
 * @since 5.1.0.0
 */
public class AbstractMethodInvokingInstrumenter
{
    private final ModuleBuilderSource moduleSource;

    protected final Map<Class, Object> resourcesDefaults = CollectionFactory.newMap();

    private final ServiceResources resources;

    private final PlasticProxyFactory proxyFactory;

    protected final Method method;

    protected final Class serviceInterface;

    protected final String serviceId;

    private final Logger logger;

    public AbstractMethodInvokingInstrumenter(ModuleBuilderSource moduleSource, Method method,
                                              ServiceResources resources, PlasticProxyFactory proxyFactory)
    {
        this.moduleSource = moduleSource;
        this.method = method;
        this.resources = resources;
        this.proxyFactory = proxyFactory;

        serviceId = resources.getServiceId();

        resourcesDefaults.put(String.class, serviceId);
        resourcesDefaults.put(ObjectLocator.class, resources);
        resourcesDefaults.put(ServiceResources.class, resources);
        logger = resources.getLogger();
        resourcesDefaults.put(Logger.class, logger);
        serviceInterface = resources.getServiceInterface();
        resourcesDefaults.put(Class.class, serviceInterface);
        resourcesDefaults.put(OperationTracker.class, resources.getTracker());
    }

    @Override
    public String toString()
    {
        Location location = proxyFactory.getMethodLocation(method);

        return location.toString();
    }

    private Object getModuleInstance()
    {
        return InternalUtils.isStatic(method) ? null : moduleSource.getModuleBuilder();
    }

    protected Object invoke(final InjectionResources injectionResources)
    {
        final String methodId = toString();

        String description = String.format("Invoking method %s", methodId);

        if (logger.isDebugEnabled())
        {
            logger.debug(description);
        }

        return resources.getTracker().invoke(description, new Invokable<Object>()
        {
            public Object invoke()
            {
                Object result = null;
                Throwable failure = null;

                try
                {
                    Object[] parameters = InternalUtils.calculateParametersForMethod(method, resources,
                            injectionResources, resources.getTracker());

                    result = method.invoke(getModuleInstance(), parameters);
                } catch (InvocationTargetException ite)
                {
                    failure = ite.getTargetException();
                } catch (Exception ex)
                {
                    failure = ex;
                }

                if (failure != null)
                    throw new RuntimeException(String.format("Exception invoking method %s: %s", methodId,
                            InternalUtils.toMessage(failure)), failure);

                return result;
            }
        });

    }
}
