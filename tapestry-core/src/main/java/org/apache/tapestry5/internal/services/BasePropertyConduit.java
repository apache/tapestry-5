// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.TypeCoercer;

import java.lang.annotation.Annotation;

/**
 * Base class for {@link org.apache.tapestry5.PropertyConduit} instances created by the {@link
 * org.apache.tapestry5.services.PropertyConduitSource}.
 */
public abstract class BasePropertyConduit implements PropertyConduit
{
    private final Class propertyType;

    private final AnnotationProvider annotationProvider;

    private final String description;

    private final TypeCoercer typeCoercer;

    public BasePropertyConduit(Class propertyType, AnnotationProvider annotationProvider, String description,
                               TypeCoercer typeCoercer)
    {
        Defense.notNull(propertyType, "propertyType");
        Defense.notNull(annotationProvider, "annotationProvider");
        Defense.notBlank(description, "description");
        Defense.notNull(typeCoercer, "typeCoercer");

        this.propertyType = propertyType;
        this.annotationProvider = annotationProvider;
        this.description = description;
        this.typeCoercer = typeCoercer;
    }

    @Override
    public final String toString()
    {
        return description;
    }

    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return annotationProvider.getAnnotation(annotationClass);
    }

    public final Class getPropertyType()
    {
        return propertyType;
    }

    public final IntegerRange range(int from, int to)
    {
        return new IntegerRange(from, to);
    }

    protected final <T> T coerce(Object value, Class<T> type)
    {
        return typeCoercer.coerce(value, type);
    }

    public final boolean invert(Object value)
    {
        return coerce(value, Boolean.class).equals(Boolean.FALSE);
    }
}
