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

package org.apache.tapestry5.ioc.internal.services;

import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.services.PropertyAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyAdapterImpl implements PropertyAdapter
{
    private final Class beanType;

    private final String name;

    private final Method readMethod;

    private final Method writeMethod;

    private final Class type;

    private final boolean castRequired;

    /**
     * Have we tried to resolve from the property name to the field yet?
     */
    private boolean fieldCheckedFor;
    /**
     * The field from the containing type that matches this property name (may be null if not found, or not checked for
     * yet).
     */
    private Field field;

    public PropertyAdapterImpl(Class beanType, String name, Class type, Method readMethod, Method writeMethod)
    {
        this.beanType = notNull(beanType, "beanType");
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

        if (result == null) result = getAnnotationFromField(annotationClass);

        return result;
    }

    private <T extends Annotation> T getAnnotationFromField(Class<T> annotationClass)
    {
        Field field = getField();

        return field == null ? null : field.getAnnotation(annotationClass);
    }


    private synchronized Field getField()
    {
        if (!fieldCheckedFor)
        {
            // There's an assumption here, that the fields match the property name (we ignore case
            // which leads to a manageable ambiguity) and that the field and the getter/setter
            // are in the same class (i.e., that we don't have a getter exposing a protected field inherted
            // from a base class, or some other oddity).

            for (Field f : beanType.getDeclaredFields())
            {
                if (f.getName().equalsIgnoreCase(name))
                {
                    field = f;
                    break;
                }
            }

            fieldCheckedFor = true;
        }

        return field;
    }

    public boolean isCastRequired()
    {
        return castRequired;
    }
}
