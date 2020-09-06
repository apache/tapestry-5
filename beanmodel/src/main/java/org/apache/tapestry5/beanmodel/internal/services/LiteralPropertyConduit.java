// Copyright 2008, 2011 The Apache Software Foundation
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
import java.lang.reflect.Type;

import org.apache.tapestry5.beanmodel.internal.InternalPropertyConduit;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.services.TypeCoercer;

/**
 * A PropertyConduit for a literal value in an expression, such as a number, or "true", "false" or "null".
 */
public class LiteralPropertyConduit extends PropertyConduitDelegate implements InternalPropertyConduit
{
    private final Class propertyType;

    private final AnnotationProvider annotationProvider;

    private final String description;

    private final Object value;

    public LiteralPropertyConduit(TypeCoercer typeCoercer, Class propertyType, AnnotationProvider annotationProvider,
            String description, Object value)
    {
        super(typeCoercer);

        this.propertyType = propertyType;
        this.annotationProvider = annotationProvider;
        this.description = description;

        this.value = value;
    }

    public Object get(Object instance)
    {
        return value;
    }

    public void set(Object instance, Object value)
    {
        throw new RuntimeException("Literal values are not updateable.");
    }

    public Class getPropertyType()
    {
        return propertyType;
    }
    
    public Type getPropertyGenericType()
    {
        return propertyType;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return annotationProvider.getAnnotation(annotationClass);
    }

    public String getPropertyName()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return description;
    }

}
