// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.lang.annotation.Annotation;

import org.apache.tapestry5.internal.InternalPropertyConduit;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;

/**
 * Companion class for {@link org.apache.tapestry5.PropertyConduit} instances created by the
 * {@link org.apache.tapestry5.services.PropertyConduitSource}.
 */
@SuppressWarnings("all")
public class PropertyConduitDelegate
{
    private final Class propertyType;

    private final String propertyName;

    private final AnnotationProvider annotationProvider;

    private final TypeCoercer typeCoercer;

    public PropertyConduitDelegate(Class propertyType, String propertyName, AnnotationProvider annotationProvider,
            TypeCoercer typeCoercer)
    {
        assert propertyType != null;
        assert annotationProvider != null;
        assert typeCoercer != null;

        this.propertyType = propertyType;
        this.propertyName = propertyName;
        this.annotationProvider = annotationProvider;
        this.typeCoercer = typeCoercer;
    }

    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return annotationProvider.getAnnotation(annotationClass);
    }

    public final Class getPropertyType()
    {
        return propertyType;
    }

    public final String getPropertyName()
    {
        return propertyName;
    }

    public final IntegerRange range(int from, int to)
    {
        return new IntegerRange(from, to);
    }

    public final <T> T coerce(Object value, Class<T> type)
    {
        return typeCoercer.coerce(value, type);
    }

    public final boolean invert(Object value)
    {
        return coerce(value, Boolean.class).equals(Boolean.FALSE);
    }
}
