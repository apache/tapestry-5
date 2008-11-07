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

import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Basic implementation of {@link org.apache.tapestry5.ioc.ObjectCreator} that handles invoking a method on the module
 * builder, and figures out the correct parameters to pass into the annotated method.
 */
public class ServiceBuilderMethodInvoker extends AbstractServiceCreator
{
    private final Method builderMethod;

    public ServiceBuilderMethodInvoker(ServiceBuilderResources resources,
                                       String creatorDescription, Method method)
    {
        super(resources, creatorDescription);

        builderMethod = method;
    }

    /**
     * Returns a map that includes (possibly) an additional mapping containing the collected configuration data. This
     * involves scanning the builder method's parameters.
     */
    private Map<Class, Object> getParameterDefaultsWithConfigurations()
    {
        return getParameterDefaultsWithConfiguration(
                builderMethod.getParameterTypes(),
                builderMethod.getGenericParameterTypes());
    }

    /**
     * Invoked from the proxy to create the actual service implementation.
     */
    public Object createObject()
    {
        // Defer getting (and possibly instantitating) the module builder until the last possible
        // moment. If the method is static, there's no need to even get the builder.

        Object moduleBuilder = InternalUtils.isStatic(builderMethod) ? null : resources
                .getModuleBuilder();

        Object result = null;
        Throwable failure = null;

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    builderMethod,
                    resources,
                    getParameterDefaultsWithConfigurations(), resources.getTracker());

            if (logger.isDebugEnabled())
                logger.debug(IOCMessages.invokingMethod(creatorDescription));

            result = builderMethod.invoke(moduleBuilder, parameters);
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
                    creatorDescription,
                    serviceId,
                    failure), failure);

        if (result == null)
            throw new RuntimeException(IOCMessages.builderMethodReturnedNull(
                    creatorDescription,
                    serviceId));

        return result;
    }

    @Override
    public String toString()
    {
        return creatorDescription;
    }
}
