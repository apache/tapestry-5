// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.internal.services.AccessableObjectAnnotationProvider;
import org.apache.tapestry5.commons.internal.services.AnnotationProviderChain;
import org.apache.tapestry5.commons.internal.services.ServiceMessages;
import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;

public class PropertyAdapterImpl implements PropertyAdapter
{
    private final ClassPropertyAdapter classAdapter;

    private final String name;

    private final Method readMethod;

    private final Method writeMethod;

    private final Class type;

    private final boolean castRequired;

    // Synchronized by this; lazily initialized
    private AnnotationProvider annotationProvider;

    private final Field field;

    private final Class declaringClass;

    PropertyAdapterImpl(ClassPropertyAdapter classAdapter, String name, Class type, Method readMethod,
                        Method writeMethod)
    {
        this.classAdapter = classAdapter;
        this.name = name;
        this.type = type;

        this.readMethod = readMethod;
        this.writeMethod = writeMethod;

        declaringClass = readMethod != null ? readMethod.getDeclaringClass() : writeMethod.getDeclaringClass();

        castRequired = readMethod != null && readMethod.getReturnType() != type;

        field = null;
    }

    PropertyAdapterImpl(ClassPropertyAdapter classAdapter, String name, Class type, Field field)
    {
        this.classAdapter = classAdapter;
        this.name = name;
        this.type = type;

        this.field = field;

        declaringClass = field.getDeclaringClass();

        castRequired = field.getType() != type;

        readMethod = null;
        writeMethod = null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Method getReadMethod()
    {
        return readMethod;
    }

    @Override
    public Class getType()
    {
        return type;
    }

    @Override
    public Method getWriteMethod()
    {
        return writeMethod;
    }

    @Override
    public boolean isRead()
    {
        return field != null || readMethod != null;
    }

    @Override
    public boolean isUpdate()
    {
        return writeMethod != null || (field != null && !isFinal(field));
    }

    private boolean isFinal(Member member)
    {
        return Modifier.isFinal(member.getModifiers());
    }

    @Override
    public Object get(Object instance)
    {
        if (field == null && readMethod == null)
        {
            throw new UnsupportedOperationException(String.format("Class %s does not provide an accessor ('getter') method for property '%s'.", toClassName(instance), name));
        }

        Throwable fail;

        try
        {
            if (field == null)
                return readMethod.invoke(instance);
            else
                return field.get(instance);
        } catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        } catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(ServiceMessages.readFailure(name, instance, fail), fail);
    }

    @Override
    public void set(Object instance, Object value)
    {
        if (field == null && writeMethod == null)
        {
            throw new UnsupportedOperationException(String.format("Class %s does not provide a mutator ('setter') method for property '%s'.",
                    toClassName(instance),
                    name
            ));
        }

        Throwable fail;

        try
        {
            if (field == null)
                writeMethod.invoke(instance, value);
            else
                field.set(instance, value);

            return;
        } catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        } catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(String.format("Error updating property '%s' of %s: %s",
                name, toClassName(instance),
                ExceptionUtils.toMessage(fail)), fail);
    }

    private String toClassName(Object instance)
    {
        return instance == null ? "<null>" : instance.getClass().getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return getAnnnotationProvider().getAnnotation(annotationClass);
    }

    /**
     * Creates (as needed) the annotation provider for this property.
     */
    private synchronized AnnotationProvider getAnnnotationProvider()
    {
        if (annotationProvider == null)
        {
            List<AnnotationProvider> providers = CollectionFactory.newList();

            if (readMethod != null)
                providers.add(new AccessableObjectAnnotationProvider(readMethod));

            if (writeMethod != null)
                providers.add(new AccessableObjectAnnotationProvider(writeMethod));

            // There's an assumption here, that the fields match the property name (we ignore case
            // which leads to a manageable ambiguity) and that the field and the getter/setter
            // are in the same class (i.e., that we don't have a getter exposing a protected field inherted
            // from a base class, or some other oddity).

            Class cursor = getBeanType();

            out:
            while (cursor != null)
            {
                for (Field f : cursor.getDeclaredFields())
                {
                    if (f.getName().equalsIgnoreCase(name))
                    {
                        providers.add(new AccessableObjectAnnotationProvider(f));

                        break out;
                    }
                }

                cursor = cursor.getSuperclass();
            }

            annotationProvider = AnnotationProviderChain.create(providers);
        }

        return annotationProvider;
    }

    @Override
    public boolean isCastRequired()
    {
        return castRequired;
    }

    @Override
    public ClassPropertyAdapter getClassAdapter()
    {
        return classAdapter;
    }

    @Override
    public Class getBeanType()
    {
        return classAdapter.getBeanType();
    }

    @Override
    public boolean isField()
    {
        return field != null;
    }

    @Override
    public Field getField()
    {
        return field;
    }

    @Override
    public Class getDeclaringClass()
    {
        return declaringClass;
    }
}
