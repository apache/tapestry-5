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

package org.apache.tapestry5.plastic;

import java.lang.reflect.Method;

/**
 * A representation of the invocation of a method that allows the behavior of the method to be advised: either by
 * changing parameter values, or by changing the return value, or by catch or throwing different exceptions. Provides
 * access to annotations on the advised method.
 * 
 * @see MethodAdvice
 */
public interface MethodInvocation extends MethodInvocationResult, AnnotationAccess
{
    /** The instance on which the method was originally invoked. */
    Object getInstance();

    InstanceContext getInstanceContext();

    /**
     * Proceed with the method invocation, either chaining into the next {@link MethodAdvice} added to the method, or
     * ultimately into the actual method implementation. The method may throw a checked exception, which will be caught
     * and be reported as {@link #didThrowCheckedException()}.
     * 
     * @return this method invocation, for a fluent API
     */
    MethodInvocation proceed();

    /**
     * Overrides the return value of the method. The value provided will be cast to the actual return type
     * (or, if the return type is a primitive value, the value will be cast to the corresponding wrapper type and then
     * converted to a primitive).
     *
     * Overriding the return value clears any checked exception.
     * 
     * @param returnValue
     * @return this method invocation, for a fluent API
     * @throws NullPointerException
     *             if the method's return type is a primitive and null is provided
     */
    MethodInvocation setReturnValue(Object returnValue);

    /**
     * Returns the parameter at the given index. Primitive types will be wrapped as they are returned.
     * 
     * @param index
     *            of parameter to access
     * @return parameter value
     */
    Object getParameter(int index);

    /**
     * Changes a parameter value. The value will be cast to the parameter's type. For primitive types, the
     * value will be cast to the corresponding wrapper type.
     * 
     * @param index
     *            index of parameter to modify
     * @param newValue
     *            new value for parameter
     * @return this method invocation, for a fluent API
     */
    MethodInvocation setParameter(int index, Object newValue);

    /**
     * Sets the checked exception; this can be used to indicate failure for the method, or
     * to cancel the thrown exception (by setting the exception to null).
     * 
     * @param exception
     *            new checked exception, or null
     * @return this method invocation, for a fluent API
     */
    MethodInvocation setCheckedException(Exception exception);

    /** Returns the method being invoked. */
    Method getMethod();
}
