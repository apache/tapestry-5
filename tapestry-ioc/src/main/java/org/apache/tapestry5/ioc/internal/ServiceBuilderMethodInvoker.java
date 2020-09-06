// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.Method;

/**
 * Basic implementation of {@link org.apache.tapestry5.commons.ObjectCreator} that handles invoking a method on the module
 * builder, and figures out the correct parameters to pass into the annotated method.
 */
public class ServiceBuilderMethodInvoker extends AbstractServiceCreator
{
    private final Method builderMethod;

    public ServiceBuilderMethodInvoker(ServiceBuilderResources resources, String creatorDescription, Method method)
    {
        super(resources, creatorDescription);

        builderMethod = method;
    }

    private ObjectCreator<Object> plan;

    private ObjectCreator<Object> getPlan()
    {
        if (plan == null)
        {
            // Defer getting (and possibly instantiating) the module instance until the last possible
            // moment. If the method is static, there's no need to even get the builder.

            final Object moduleInstance = InternalUtils.isStatic(builderMethod) ? null : resources.getModuleBuilder();

            plan = InternalUtils.createMethodInvocationPlan(resources.getTracker(), resources, createInjectionResources(), logger, "Constructing service implementation via " + creatorDescription, moduleInstance, builderMethod);
        }

        return plan;
    }

    /**
     * Invoked from the proxy to create the actual service implementation.
     */
    @Override
    public Object createObject()
    {
        Object result = getPlan().createObject();

        if (result == null)
        {
            throw new RuntimeException(String.format("Builder method %s (for service '%s') returned null.", creatorDescription, serviceId));
        }

        return result;
    }

    @Override
    public String toString()
    {
        return creatorDescription;
    }
}
