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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.AnnotationAccess;

/**
 * A decorator used to create an interceptor that delegates each method's invocation to an
 * {@link org.apache.tapestry5.plastic.MethodAdvice} for advice. Advice can inspect or change method parameters, inspect or
 * change the method's return value, and inspect and change thrown exceptions (checked and unchecked).
 */
public interface AspectDecorator
{
    /**
     * Creates a builder that can be used to create the interceptor. This is used when only some of the methods need to
     * be advised, or when different methods need to receive different advice, or when multiple advice is to be
     * applied.
     *
     * @param serviceInterface defines the interface of the interceptor and the delegate
     * @param delegate         the object on which methods will be invokes
     * @param description      used as the toString() of the interceptor unless toString() is part of the service
     *                         interface
     * @param <T> the type of the service interface.
     * @return a builder that can be used to generate the final interceptor
     */
    <T> AspectInterceptorBuilder<T> createBuilder(Class<T> serviceInterface, T delegate, String description);

    /**
     * Creates a builder that can be used to create the interceptor. This is used when only some of the methods need to
     * be advised, or when different methods need to receive different advice, or when multiple advice is to be
     * applied.
     *
     * @param serviceInterface defines the interface of the interceptor and the delegate
     * @param delegate         the object on which methods will be invokes
     * @param annotationAccess provides access to combined annotations of the underlying service
     *                         and service interface
     * @param description      used as the toString() of the interceptor unless toString() is part of the service
     *                         interface
     * @param <T> the type of the service interface.
     * @return a builder that can be used to generate the final interceptor
     */
    <T> AspectInterceptorBuilder<T> createBuilder(Class<T> serviceInterface, T delegate,
                                                  AnnotationAccess annotationAccess, String description);
}
