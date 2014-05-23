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
import java.lang.reflect.Modifier;
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

    private final ClassInstantiatorImpl<T> parent;

    private final Class withType;

    private final Object withValue;

    ClassInstantiatorImpl(Class<T> clazz, Constructor ctor, StaticContext staticContext)
    {
        this(clazz, ctor, staticContext, null, null, null);
    }

    private <W> ClassInstantiatorImpl(Class clazz, Constructor ctor, StaticContext staticContext,
            ClassInstantiatorImpl<T> parent, Class<W> withType, W withValue)
    {
        this.clazz = clazz;
        this.ctor = ctor;
        this.staticContext = staticContext;
        this.parent = parent;
        this.withType = withType;
        this.withValue = withValue;
    }

    @Override
    public <V> ClassInstantiator<T> with(Class<V> valueType, V instanceContextValue)
    {
        assert valueType != null;
        assert instanceContextValue != null;

        Object existing = find(valueType);

        if (existing != null)
            throw new IllegalStateException(String.format(
                    "An instance context value of type %s has already been added.", valueType.getName()));

        // A little optimization: the new CI doesn't need this CI as a parent, if this CI has no type/value pair
        
        return new ClassInstantiatorImpl(clazz, ctor, staticContext, withType == null ? null : this, valueType,
                instanceContextValue);
    }

    @Override
    public <V> V get(Class<V> valueType)
    {
        V result = find(valueType);

        if (result == null)
            throw new IllegalArgumentException(String.format(
                    "Instance context for class %s does not contain a value for type %s.", clazz.getName(), valueType));

        return result;
    }

    private <V> V find(Class<V> valueType)
    {
        ClassInstantiatorImpl cursor = this;

        while (cursor != null)
        {
            if (cursor.withType == valueType) { return valueType.cast(cursor.withValue); }

            cursor = cursor.parent;
        }

        return null;
    }

    @Override
    public T newInstance()
    {
        if (Modifier.isAbstract(clazz.getModifiers()))
            throw new IllegalStateException(String.format("Class %s is abstract and can not be instantiated.",
                    clazz.getName()));

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

    @Override
    public Class<T> getInstanceType()
    {
        return clazz;
    }

    @Override
    public String toString()
    {
        return String.format("ClassInstantiator[%s]", clazz.getName());
    }
}
