// Copyright 2013 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

final public class TestAdvice implements MethodAdvice {

    public static final String ANNOTATION_FOUND = "Annotation found!";

    @Override
    public void advise(MethodInvocation invocation) {

        final Method method = invocation.getMethod();
        boolean annotationFoundInMethod = checkAnnotation(method.getAnnotation(Advise.class));
        boolean annotationFoundThroughAnnotationProvider = checkAnnotation(invocation.getAnnotation(Advise.class));
        IntermediateType parameterAnnotation = null;
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations.length > 0 && parameterAnnotations[0].length > 0) {
            parameterAnnotation = (IntermediateType) parameterAnnotations[0][0];
        }
        boolean annotationParameter = parameterAnnotation != null && parameterAnnotation.value() == String.class;
        
        if (annotationFoundInMethod && annotationFoundThroughAnnotationProvider && annotationParameter) 
        {
            invocation.setReturnValue(ANNOTATION_FOUND);
        }
        else {
            invocation.proceed();
        }
        
    }

    private boolean checkAnnotation(Advise annotation)
    {
        return annotation != null && "id".equals(annotation.id()) && NonAnnotatedServiceInterface.class.equals(annotation.serviceInterface());
    }
    
}