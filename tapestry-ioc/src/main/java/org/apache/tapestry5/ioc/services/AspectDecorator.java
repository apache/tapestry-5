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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.MethodAdvice;

/**
 * A decorator used to create an interceptor that delegates each method's invocation to an {@link
 * org.apache.tapestry5.ioc.MethodAdvice} for advice.  Advice can inspect or change method parameters, inspect or change
 * the method's return value, and inspect and change thrown exceptions (checked and unchecked).
 */
public interface AspectDecorator
{
    /**
     * Intercepts method invocations on the delegate and passes them through the provided aspect. Note that the advice
     * <em>must</em> be thread-safe.
     *
     * @param serviceInterface defines the interface of the interceptor and delegate
     * @param delegate         the object on which methods will be invoked
     * @param advice           intercepts the method invocations on the delegate
     * @param description      used as the toString() of the returned interceptor, unless toString() is part of the
     *                         service interface
     * @return the interceptor, wrapping the delegate with all the advice
     */
    <T> T build(Class<T> serviceInterface, T delegate, MethodAdvice advice, String description);

    /**
     * Creates a builder that can be used to create the interceptor.  This is used when only some of the methods need to
     * be advised, or when different methods need to recieve different advice, or when multiple advice is to be
     * applied.
     *
     * @param serviceInterface defines the interface of the interceptor and the delegate
     * @param delegate         the object on which methods will be invokes
     * @param description      used as the toString() of the interceptor unless toString() is part of the service
     *                         interface
     * @return a builder that can be used to generate the final interceptor
     */
    <T> AspectInterceptorBuilder<T> createBuilder(Class<T> serviceInterface, T delegate, String description);
}
