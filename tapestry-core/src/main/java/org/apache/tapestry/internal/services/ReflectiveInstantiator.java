// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.Component;

import java.lang.reflect.Constructor;

/**
 * Implementation of {@link Instantiator} based on a class, a list of parameters to the class'
 * constructor, and a instance of {@link org.apache.tapestry.internal.InternalComponentResources}.
 * This implementation uses a little reflection to instantiate the instance.
 */
public class ReflectiveInstantiator implements Instantiator
{
    private final ComponentModel _componentModel;

    private final Constructor _constructor;

    private final Object[] _constructorParameters;

    /**
     * Creates a new instance that will instantiate the given class. The
     *
     * @param componentModel model defining the behavior of the component
     * @param instanceClass  class to instantiate
     * @param parameters     passed to the constructor; the first instance is ignored (and overriden) as the
     *                       {@link org.apache.tapestry.internal.InternalComponentResources} instance.
     */
    ReflectiveInstantiator(ComponentModel componentModel, Class instanceClass, Object[] constructorParameters)
    {
        _componentModel = componentModel;
        _constructorParameters = constructorParameters;

        _constructor = findConstructor(instanceClass, constructorParameters.length);

    }

    @Override
    public String toString()
    {
        return String.format("ReflectiveInstantiator[%s]", _constructor);
    }

    static Constructor findConstructor(Class instanceClass, int parameterCount)
    {
        for (Constructor c : instanceClass.getConstructors())
        {
            if (c.getParameterTypes().length == parameterCount) return c;
        }

        throw new RuntimeException(ServicesMessages.noConstructorFound(instanceClass));
    }

    public Component newInstance(InternalComponentResources resources)
    {
        // Hm. Is it faster to clone the parameters, or to synchronize this method?

        Object[] parameters = _constructorParameters.clone();

        parameters[0] = resources;

        try
        {
            return (Component) _constructor.newInstance(parameters);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public ComponentModel getModel()
    {
        return _componentModel;
    }

}
