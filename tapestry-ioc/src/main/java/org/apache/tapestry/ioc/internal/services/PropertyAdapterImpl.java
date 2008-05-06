// Copyright 2006, 2008 The Apache Software Foundation
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyAdapterImpl implements PropertyAdapter
{
    private final String name;

    private final Method readMethod;

    private final Method writeMethod;

    private final Class type;

    private final boolean castRequired;

    public PropertyAdapterImpl(String name, Class type, Method readMethod, Method writeMethod)
    {
        this.name = notBlank(name, "name");
        this.type = notNull(type, "type");

        this.readMethod = readMethod;
        this.writeMethod = writeMethod;

        castRequired = readMethod != null && readMethod.getReturnType() != type;
    }

    public String getName()
    {
        return name;
    }

    public Method getReadMethod()
    {
        return readMethod;
    }

    public Class getType()
    {
        return type;
    }

    public Method getWriteMethod()
    {
        return writeMethod;
    }

    public boolean isRead()
    {
        return readMethod != null;
    }

    public boolean isUpdate()
    {
        return writeMethod != null;
    }

    public Object get(Object instance)
    {
        if (readMethod == null)
            throw new UnsupportedOperationException(ServiceMessages.readNotSupported(instance, name));

        Throwable fail;

        try
        {
            return readMethod.invoke(instance);
        }
        catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(ServiceMessages.readFailure(name, instance, fail), fail);
    }

    public void set(Object instance, Object value)
    {
        if (writeMethod == null)
            throw new UnsupportedOperationException(ServiceMessages.writeNotSupported(instance, name));

        Throwable fail;

        try
        {
            writeMethod.invoke(instance, value);

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

        throw new RuntimeException(ServiceMessages.writeFailure(name, instance, fail), fail);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        T result = readMethod != null ? readMethod.getAnnotation(annotationClass) : null;

        if (result == null && writeMethod != null) result = writeMethod.getAnnotation(annotationClass);

        return result;
    }

    public boolean isCastRequired()
    {
        return castRequired;
    }
}
