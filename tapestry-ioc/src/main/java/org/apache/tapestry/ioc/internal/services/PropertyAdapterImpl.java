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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.services.PropertyAdapter;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyAdapterImpl implements PropertyAdapter
{
    private final String _name;

    private final Method _readMethod;

    private final Method _writeMethod;

    private final Class _type;

    public PropertyAdapterImpl(String name, Class type, Method readMethod, Method writeMethod)
    {
        _name = notBlank(name, "name");
        _type = notNull(type, "type");

        _readMethod = readMethod;
        _writeMethod = writeMethod;
    }

    public PropertyAdapterImpl(PropertyDescriptor descriptor)
    {
        this(descriptor.getName(), descriptor.getPropertyType(), descriptor.getReadMethod(),
             descriptor.getWriteMethod());
    }

    public String getName()
    {
        return _name;
    }

    public Method getReadMethod()
    {
        return _readMethod;
    }

    public Class getType()
    {
        return _type;
    }

    public Method getWriteMethod()
    {
        return _writeMethod;
    }

    public boolean isRead()
    {
        return _readMethod != null;
    }

    public boolean isUpdate()
    {
        return _writeMethod != null;
    }

    public Object get(Object instance)
    {
        if (_readMethod == null)
            throw new UnsupportedOperationException(ServiceMessages.readNotSupported(
                    instance,
                    _name));

        Throwable fail = null;

        try
        {
            return _readMethod.invoke(instance);
        }
        catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(ServiceMessages.readFailure(_name, instance, fail), fail);
    }

    public void set(Object instance, Object value)
    {
        if (_writeMethod == null)
            throw new UnsupportedOperationException(ServiceMessages.writeNotSupported(
                    instance,
                    _name));

        Throwable fail = null;

        try
        {
            _writeMethod.invoke(instance, value);

            return;
        }
        catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(ServiceMessages.writeFailure(_name, instance, fail), fail);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        T result = _readMethod != null ? _readMethod.getAnnotation(annotationClass) : null;

        if (result == null && _writeMethod != null)
            result = _writeMethod.getAnnotation(annotationClass);

        return result;
    }
}
