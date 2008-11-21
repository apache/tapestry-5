// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.AnnotationProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

/**
 * Provides access to annotations of an accessable object such as a {@link java.lang.reflect.Method} or {@link
 * java.lang.reflect.Field}.
 */
public class AccessableObjectAnnotationProvider implements AnnotationProvider
{
    private final AccessibleObject object;

    public AccessableObjectAnnotationProvider(AccessibleObject object)
    {
        this.object = object;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return object.getAnnotation(annotationClass);
    }

    @Override
    public String toString()
    {
        return String.format("AnnotationProvider[%s]", object);
    }
}
