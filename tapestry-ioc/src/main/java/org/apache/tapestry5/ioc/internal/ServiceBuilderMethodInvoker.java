// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
     * Invoked from the proxy to create the actual service implementation.
     */
    public Object createObject()
    {
        // Defer getting (and possibly instantitating) the module instance until the last possible
        // moment. If the method is static, there's no need to even get the builder.

        Object moduleInstance = InternalUtils.isStatic(builderMethod)
                                ? null
                                : resources.getModuleBuilder();

        Object result = null;
        Throwable failure = null;

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    builderMethod,
                    resources,
                    createInjectionResources(), resources.getTracker());

            if (logger.isDebugEnabled())
                logger.debug(IOCMessages.invokingMethod(creatorDescription));

            result = builderMethod.invoke(moduleInstance, parameters);
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
