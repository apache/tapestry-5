// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A service creator based on an implementation class' constructor, rather than a service builder method.
 */
public class ConstructorServiceCreator extends AbstractServiceCreator
{
    private final Constructor constructor;

    public ConstructorServiceCreator(ServiceBuilderResources resources, String creatorDescription,
                                     Constructor constructor)
    {
        super(resources, creatorDescription);

        this.constructor = constructor;
    }

    @Override
    public String toString()
    {
        return creatorDescription;
    }

    public Object createObject()
    {
        Throwable failure = null;
        Object result = null;

        InternalUtils.validateConstructorForAutobuild(constructor);

        InjectionResources injectionResources = createInjectionResources();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForConstructor(constructor, resources,
                                                                                  injectionResources,
                                                                                  resources.getTracker());

            if (logger.isDebugEnabled()) logger.debug(IOCMessages.invokingConstructor(creatorDescription));

            result = constructor.newInstance(parameters);

            InternalUtils.injectIntoFields(result, resources, injectionResources, resources.getTracker());
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
            throw new RuntimeException(IOCMessages.constructorError(creatorDescription, serviceId, failure), failure);

        InternalUtils.invokePostInjectionMethods(result, resources, injectionResources, resources.getTracker());

        return result;
    }
}
