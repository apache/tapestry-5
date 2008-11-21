// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A wrapper around a decorator method.
 */
public class ServiceDecoratorImpl implements ServiceDecorator
{
    private final ModuleBuilderSource moduleBuilderSource;

    private final String serviceId;

    private final Map<Class, Object> parameterDefaults = newMap();

    private final Logger logger;

    private final ServiceResources resources;

    private final ClassFactory classFactory;

    private final Method decoratorMethod;

    private final Class serviceInterface;

    public ServiceDecoratorImpl(Method method, ModuleBuilderSource moduleBuilderSource,
                                ServiceResources resources, ClassFactory classFactory)
    {
        serviceId = resources.getServiceId();
        decoratorMethod = method;
        this.moduleBuilderSource = moduleBuilderSource;
        this.resources = resources;
        serviceInterface = resources.getServiceInterface();
        logger = resources.getLogger();
        this.classFactory = classFactory;

        parameterDefaults.put(String.class, serviceId);
        parameterDefaults.put(ObjectLocator.class, resources);
        parameterDefaults.put(ServiceResources.class, resources);
        parameterDefaults.put(Logger.class, logger);
        parameterDefaults.put(Class.class, serviceInterface);
    }

    @Override
    public String toString()
    {
        return classFactory.getMethodLocation(decoratorMethod).toString();
    }

    private String methodId()
    {
        return InternalUtils.asString(decoratorMethod, classFactory);
    }

    public Object createInterceptor(Object delegate)
    {
        // Create a copy of the parameters map so that Object.class points to the delegate instance.

        Map<Class, Object> parameterDefaults = newMap(this.parameterDefaults);
        parameterDefaults.put(Object.class, delegate);
        parameterDefaults.put(serviceInterface, delegate);

        if (logger.isDebugEnabled()) logger.debug(IOCMessages.invokingMethod(methodId()));

        Object result = null;
        Throwable failure = null;

        Object moduleBuilder = InternalUtils.isStatic(decoratorMethod) ? null
                               : moduleBuilderSource.getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    decoratorMethod,
                    resources,
                    parameterDefaults, resources.getTracker());

            result = decoratorMethod.invoke(moduleBuilder, parameters);
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
            throw new RuntimeException(IOCMessages.decoratorMethodError(
                    decoratorMethod,
                    serviceId,
                    failure), failure);

        if (result != null && !serviceInterface.isInstance(result))
        {
            logger.warn(IOCMessages.decoratorReturnedWrongType(
                    decoratorMethod,
                    serviceId,
                    result,
                    serviceInterface));

            // Change the result to null so that we won't use the interceptor,
            // and so that ClassCastExceptions don't occur later down the pipeline.

            result = null;
        }

        return result;
    }
}
