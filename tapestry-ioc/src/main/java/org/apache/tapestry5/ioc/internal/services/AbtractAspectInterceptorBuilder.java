// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

public abstract class AbtractAspectInterceptorBuilder<T> implements AspectInterceptorBuilder<T>
{
    protected final AnnotationAccess annotationAccess;

    public AbtractAspectInterceptorBuilder(AnnotationAccess annotationAccess)
    {
        this.annotationAccess = annotationAccess;
    }

    @Override
    public AnnotationProvider getClassAnnotationProvider()
    {
        return annotationAccess.getClassAnnotationProvider();
    }

    @Override
    public AnnotationProvider getMethodAnnotationProvider(String methodName, Class... parameterTypes)
    {
        return annotationAccess.getMethodAnnotationProvider(methodName, parameterTypes);
    }

    @Override
    public <T extends Annotation> T getMethodAnnotation(Method method, Class<T> annotationType)
    {
        return getMethodAnnotationProvider(method.getName(), method.getParameterTypes()).getAnnotation(annotationType);
    }
}
