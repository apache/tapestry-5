// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.plastic.MethodHandle;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;

/**
 * Represents a bridge to an object method, when that method may not be public. A MethodAccess object
 * encapsulates the approach for invoking the method and capturing the result (either the return value,
 * or the checked exception that is thrown).
 *
 * @see TransformMethod#getAccess()
 * @see PlasticClass
 * @see PlasticMethod
 * @see MethodHandle
 * @since 5.2.0
 * @deprecated Deprecated in 5.3
 */
public interface MethodAccess
{
    /**
     * Invoke the method on the target, passing a number of arguments to the method.
     * If the method throws a RuntimeException, that is passed through unchanged.
     * If the method throws a checked exception, that will be reflected in the invocation result.
     *
     * @param target    object on which to invoke a method
     * @param arguments arguments to pass to the method
     */
    MethodInvocationResult invoke(Object target, Object... arguments);
}
