// Copyright 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.plastic.MethodAdvice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Interface used with service advisor methods to define advice. Allows advice on specific methods, or on all methods.
 */
public interface MethodAdviceReceiver extends AnnotationAccess
{

    /**
     * Advises <em>all</em> methods of the interface with the given advice.
     * @param advice the method advice to be applied.
     *
     * @since 5.3
     */
    void adviseAllMethods(MethodAdvice advice);

    /**
     * Adds advice for a specific method of the aspect interceptor being constructed.
     *
     * @param method method (of the interface for which an interceptor is being constructed) to be advised. Multiple
     *               advice is allowed for a single method; the advice will be executed in the order it is added.
     * @param advice the advice for this particular method. Advice must be threadsafe.
     * @since 5.3
     */
    void adviseMethod(Method method, MethodAdvice advice);

    /**
     * Returns the interface for which methods may be advised.
     * @return the interface class instance.
     *
     * @since 5.1.0.0
     */
    Class getInterface();

    /**
     * Gets an annotation from a method, via {@link AnnotationAccess#getMethodAnnotationProvider(String, Class...)}.
     *
     * @param <T>            type of annotation
     * @param method         method to search
     * @param annotationType type of annotation
     * @return the annotation found on the underlying implementation class (if known) or service interface, or null if
     *         not found
     */
    <T extends Annotation> T getMethodAnnotation(Method method, Class<T> annotationType);
}
