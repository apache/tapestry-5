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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * Basic implementation of {@link org.apache.tapestry.ioc.ObjectCreator} that handles invoking a
 * method on the module builder, and figures out the correct parameters to pass into the annotated
 * method.
 */
public class ServiceBuilderMethodInvoker extends AbstractServiceCreator
{
    private final Method _builderMethod;

    public ServiceBuilderMethodInvoker(ServiceBuilderResources resources,
            String creatorDescription, Method method)
    {
        super(resources, creatorDescription);

        _builderMethod = method;
    }

    /**
     * Returns a map that includes (possibly) an additional mapping containing the collected
     * configuration data. This involves scanning the builder method's parameters.
     */
    private Map<Class, Object> getParameterDefaultsWithConfigurations()
    {
        return getParameterDefaultsWithConfiguration(
                _builderMethod.getParameterTypes(),
                _builderMethod.getGenericParameterTypes());
    }

    /**
     * Invoked from the proxy to create the actual service implementation.
     */
    public Object createObject()
    {
        // Defer getting (and possibly instantitating) the module builder until the last possible
        // moment. If the method is static, there's no need to even get the builder.

        Object moduleBuilder = InternalUtils.isStatic(_builderMethod) ? null : _resources
                .getModuleBuilder();

        Object result = null;
        Throwable failure = null;

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    _builderMethod,
                    _resources,
                    getParameterDefaultsWithConfigurations());

            if (_logger.isDebugEnabled())
                _logger.debug(IOCMessages.invokingMethod(_creatorDescription));

            result = _builderMethod.invoke(moduleBuilder, parameters);
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
            throw new RuntimeException(IOCMessages.builderMethodError(
                    _creatorDescription,
                    _serviceId,
                    failure), failure);

        if (result == null)
            throw new RuntimeException(IOCMessages.builderMethodReturnedNull(
                    _creatorDescription,
                    _serviceId));

        return result;
    }
}