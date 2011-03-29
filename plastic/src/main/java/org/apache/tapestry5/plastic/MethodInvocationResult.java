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

package org.apache.tapestry5.plastic;

/**
 * The result of a {@linkplain MethodHandle#invoke(Object, Object...) method invocation}, which
 * encapsulates the actual return value (if any), as well as any checked exception.
 */
public interface MethodInvocationResult
{
    /**
     * Returns the actual value returned from the method invocation, if any. This will be null
     * if the invocation threw a checked exception, or the method itself is void.
     */
    Object getReturnValue();

    /**
     * If the invocation threw a checked exception, then this method throws that exception wrapped
     * as the cause of a new RuntimeException. Otherwise, this method does nothing.
     */
    void rethrow();

    /** Returns true if the method invocation threw a checked exception. */
    boolean didThrowCheckedException();

    /**
     * Retrieves the checked exception assignable to the indicated type, or null if
     * the invocation did not throw a checked exception or the thrown exception is not
     * assignable.
     */
    <T extends Throwable> T getCheckedException(Class<T> exceptionType);
}
