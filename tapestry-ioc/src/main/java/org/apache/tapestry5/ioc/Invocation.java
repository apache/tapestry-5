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

package org.apache.tapestry5.ioc;

/**
 * A method invocation passed to a {@link org.apache.tapestry5.ioc.MethodAdvice}.
 */
public interface Invocation
{
    /**
     * Returns the name of the method being invoked.
     */
    String getMethodName();

    /**
     * Returns the type of the method result, which may be a primitive type (i.e., int.class) or even void
     * (void.class).
     */
    Class getResultType();

    /**
     * Returns the number of parameters passed to the method.
     */
    int getParameterCount();

    /**
     * Returns the type of the parameter at the index.
     */
    Class getParameterType(int index);

    /**
     * Returns the indicated parameter (may return null if the parameter is null).
     */
    Object getParameter(int index);

    /**
     * Replaces a parameter in the invocation.
     *
     * @param index        of parameter to update
     * @param newParameter new parameter value (may be null)
     */
    void override(int index, Object newParameter);

    /**
     * Proceed with the invocation of the advised method.  If the invocation results in a <em>runtime</em> exception,
     * that is thrown. A checked exception is detected by invoking {@link #isFail()}.
     */
    void proceed();

    /**
     * If true, then the proceeded invocation threw a checked exception.
     */
    boolean isFail();

    /**
     * After invoking {@link #proceed()}, used to obtain the thrown (checked) exception, if assignable to the provided
     * type.
     *
     * @param throwableClass the type of exception to match
     * @return the exception, if the proceeded invocation threw a checked exception, and the exception is assignable to
     *         the provided type.  In other cases, null is returned.
     */
    <T extends Throwable> T getThrown(Class<T> throwableClass);

    /**
     * Overrides the thrown exception. The passed exception should be a checked exception of the method. Note that for
     * runtime exceptions, or even {@link Error}s, those can just be thrown. Sets the fail flag.
     *
     * @param thrown
     * @throws IllegalArgumentException if thrown is null, or not a declared exception of the method
     */
    void overrideThrown(Exception thrown);

    /**
     * The return value after {@link #proceed()}, which may be null.
     */
    Object getResult();

    /**
     * Overrides the result. Clears the thrown exception (if any).
     */
    void overrideResult(Object newResult);
}
