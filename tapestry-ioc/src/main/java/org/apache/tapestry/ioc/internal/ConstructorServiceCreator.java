// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * A service creator based on an implementation class' constructor, rather than a service builder
 * method.
 */
public class ConstructorServiceCreator extends AbstractServiceCreator
{
    private final Constructor _constructor;

    public ConstructorServiceCreator(ServiceBuilderResources resources, String creatorDescription,
                                     Constructor constructor)
    {
        super(resources, creatorDescription);

        _constructor = constructor;
    }

    public Object createObject()
    {
        Throwable failure = null;

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForConstructor(
                    _constructor,
                    _resources,
                    getParameterDefaultsWithConfigurations());

            if (_logger.isDebugEnabled())
                _logger.debug(IOCMessages.invokingConstructor(_creatorDescription));

            return _constructor.newInstance(parameters);
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        throw new RuntimeException(IOCMessages.constructorError(
                _creatorDescription,
                _serviceId,
                failure), failure);
    }

    /**
     * Returns a map that includes (possibly) an additional mapping containing the collected
     * configuration data. This involves scanning the constructor's parameters.
     */
    private Map<Class, Object> getParameterDefaultsWithConfigurations()
    {
        return getParameterDefaultsWithConfiguration(_constructor.getParameterTypes(), _constructor
                .getGenericParameterTypes());
    }
}
