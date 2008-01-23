// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.services.PropertyConduitSource;

import java.lang.annotation.Annotation;

/**
 * Base class for {@link PropertyConduit} instances created by the {@link PropertyConduitSource}.
 */
public abstract class BasePropertyConduit implements PropertyConduit
{
    private final Class _propertyType;

    private final AnnotationProvider _annotationProvider;

    private final String _description;

    public BasePropertyConduit(final Class propertyType,
                               final AnnotationProvider annotationProvider, final String description)
    {
        _propertyType = propertyType;
        _annotationProvider = annotationProvider;
        _description = description;
    }

    @Override
    public String toString()
    {
        return _description;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return _annotationProvider.getAnnotation(annotationClass);
    }

    public Class getPropertyType()
    {
        return _propertyType;
    }
}
