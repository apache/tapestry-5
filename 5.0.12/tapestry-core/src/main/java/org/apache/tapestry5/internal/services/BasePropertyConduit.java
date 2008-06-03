// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.Defense;

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

    public BasePropertyConduit(Class propertyType, AnnotationProvider annotationProvider, String description)
    {
        Defense.notNull(propertyType, "propertyType");
        Defense.notNull(annotationProvider, "annotationProvider");
        Defense.notBlank(description, "description");

        this.propertyType = propertyType;
        this.annotationProvider = annotationProvider;
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return annotationProvider.getAnnotation(annotationClass);
    }

    public Class getPropertyType()
    {
        return propertyType;
    }
}
