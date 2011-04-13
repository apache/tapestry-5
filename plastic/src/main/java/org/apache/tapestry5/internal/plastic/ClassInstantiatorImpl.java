// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.InstanceContext;

@SuppressWarnings("all")
public class ClassInstantiatorImpl<T> implements ClassInstantiator<T>, InstanceContext
{
    private final Class clazz;

    private final Constructor<T> ctor;

    private final StaticContext staticContext;

    // Keeping a whole HashMap around for just one or two values feels wasteful, perhaps
    // come up with something else later.

    private final Map instanceContextMap;

    ClassInstantiatorImpl(Class<T> clazz, Constructor ctor, StaticContext staticContext)
    {
        this(clazz, ctor, staticContext, null);
    }

    private ClassInstantiatorImpl(Class clazz, Constructor ctor, StaticContext staticContext, Map instanceContextMap)
    {
        this.clazz = clazz;
        this.ctor = ctor;
        this.staticContext = staticContext;
        this.instanceContextMap = instanceContextMap;
    }

    public <V> ClassInstantiator<T> with(Class<V> valueType, V instanceContextValue)
    {
        assert valueType != null;
        assert instanceContextValue != null;

        Object existing = getFromMap(valueType);

        if (existing != null)
            throw new IllegalStateException(String.format(
                    "An instance context value of type %s has already been added.", valueType.getName()));

        Map newMap = instanceContextMap == null ? new HashMap() : new HashMap(instanceContextMap);

        newMap.put(valueType, instanceContextValue);

        return new ClassInstantiatorImpl(clazz, ctor, staticContext, newMap);
    }

    public <T> T get(Class<T> valueType)
    {
        T result = getFromMap(valueType);

        if (result == null)
            throw new IllegalArgumentException(String.format(
                    "Instance context for class %s does not contain a value for type %s.", clazz.getName(), valueType));

        return result;
    }

    private <T> T getFromMap(Class<T> valueType)
    {
        return instanceContextMap == null ? null : valueType.cast(instanceContextMap.get(valueType));
    }

    public T newInstance()
    {
        try
        {
            return ctor.newInstance(staticContext, this);
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(String.format("Unable to instantiate instance of transformed class %s: %s",
                    clazz.getName(), PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    public Class<T> getInstanceType()
    {
        return clazz;
    }

    public String toString()
    {
        return String.format("ClassInstantiator[%s]", clazz.getName());
    }
}
