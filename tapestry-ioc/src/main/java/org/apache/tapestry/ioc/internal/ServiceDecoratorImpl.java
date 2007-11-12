// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.ioc.ModuleBuilderSource;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceResources;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A wrapper around a decorator method.
 */
public class ServiceDecoratorImpl implements ServiceDecorator
{
    private final ModuleBuilderSource _moduleBuilderSource;

    private final String _serviceId;

    private final Map<Class, Object> _parameterDefaults = newMap();

    private final Logger _logger;

    private final ServiceResources _resources;

    private final ClassFactory _classFactory;

    private final Method _decoratorMethod;

    private final Class _serviceInterface;

    public ServiceDecoratorImpl(Method method, ModuleBuilderSource moduleBuilderSource,
                                ServiceResources resources, ClassFactory classFactory)
    {
        _serviceId = resources.getServiceId();
        _decoratorMethod = method;
        _moduleBuilderSource = moduleBuilderSource;
        _resources = resources;
        _serviceInterface = resources.getServiceInterface();
        _logger = resources.getLogger();
        _classFactory = classFactory;

        _parameterDefaults.put(String.class, _serviceId);
        _parameterDefaults.put(ServiceResources.class, resources);
        _parameterDefaults.put(Logger.class, _logger);
        _parameterDefaults.put(Class.class, _serviceInterface);

    }

    private String methodId()
    {
        return InternalUtils.asString(_decoratorMethod, _classFactory);
    }

    public Object createInterceptor(Object delegate)
    {
        // Create a copy of the parameters map so that Object.class points to the delegate instance.

        Map<Class, Object> parameterDefaults = newMap(_parameterDefaults);
        parameterDefaults.put(Object.class, delegate);

        if (_logger.isDebugEnabled()) _logger.debug(IOCMessages.invokingMethod(methodId()));

        Object result = null;
        Throwable failure = null;

        Object moduleBuilder = InternalUtils.isStatic(_decoratorMethod) ? null
                               : _moduleBuilderSource.getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    _decoratorMethod,
                    _resources,
                    parameterDefaults);

            result = _decoratorMethod.invoke(moduleBuilder, parameters);
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
                    _decoratorMethod,
                    _serviceId,
                    failure), failure);

        if (result != null && !_serviceInterface.isInstance(result))
        {
            _logger.warn(IOCMessages.decoratorReturnedWrongType(
                    _decoratorMethod,
                    _serviceId,
                    result,
                    _serviceInterface));

            // Change the result to null so that we won't use the interceptor,
            // and so that ClassCastExceptions don't occur later down the pipeline.

            result = null;
        }

        return result;
    }
}
