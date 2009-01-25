// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
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

    private final ClassFactory classFactory;

    protected final Method method;

    protected final Class serviceInterface;

    protected final String serviceId;
    private final Logger logger;

    public AbstractMethodInvokingInstrumenter(
            ModuleBuilderSource moduleSource, Method method, ServiceResources resources, ClassFactory classFactory)
    {
        this.moduleSource = moduleSource;
        this.method = method;
        this.resources = resources;
        this.classFactory = classFactory;

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
        return classFactory.getMethodLocation(method).toString();
    }

    private Object getModuleInstance()
    {
        return InternalUtils.isStatic(method)
               ? null
               : moduleSource.getModuleBuilder();
    }

    protected Object invoke(InjectionResources injectionResources)
    {
        Object result = null;
        Throwable failure = null;

        if (logger.isDebugEnabled())
            logger.debug(String.format("Invoking method %s", this));

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    method,
                    resources,
                    injectionResources, resources.getTracker());

            result = method.invoke(getModuleInstance(), parameters);
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        if (failure != null)
            throw new RuntimeException(
                    String.format("Exception invoking method %s: %s",
                                  this,
                                  InternalUtils.toMessage(failure)),
                    failure);

        return result;
    }
}
