// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services;

/**
 * Captures the result of invoking a method.
 * 
 * @since 5.2.0
 */
public interface MethodInvocationResult
{
    /**
     * The return value from the method invocation. This will be null if the method returns null,
     * is a void method, or if a checked exception was thrown by the method.
     */
    Object getReturnValue();

    /**
     * If true, then the method invocation ended with a checked exception being thrown.
     */
    boolean isFail();

    /**
     * If the invocation threw a checked exception, this method will wrap that exception in a
     * RuntimeException and throw that. For most code that doesn't specifically care about
     * the thrown exception, this method should be invoked before continuing on to
     * examine {@link #getReturnValue()}.
     */
    void rethrow();

    /**
     * If {@link #isFail()} is true, this method provides access to the actual checked exception that was thrown.
     * 
     * @param throwableClass
     *            the type of exception to match
     * @return the exception, if the method invocation threw a checked exception, and the exception is assignable to
     *         the provided type. In other cases, null is returned.
     */
    <T extends Throwable> T getThrown(Class<T> throwableClass);
}
