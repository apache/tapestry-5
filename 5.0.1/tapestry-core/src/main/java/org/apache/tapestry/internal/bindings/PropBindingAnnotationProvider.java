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

package org.apache.tapestry.internal.bindings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.tapestry.AnnotationProvider;

public class PropBindingAnnotationProvider implements AnnotationProvider
{
    private final Method _readMethod;

    private final Method _writeMethod;

    public PropBindingAnnotationProvider(final Method readMethod, final Method writeMethod)
    {
        _readMethod = readMethod;
        _writeMethod = writeMethod;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        T result = _readMethod == null ? null : _readMethod.getAnnotation(annotationClass);

        if (result == null && _writeMethod != null)
            result = _writeMethod.getAnnotation(annotationClass);

        return result;
    }
}
