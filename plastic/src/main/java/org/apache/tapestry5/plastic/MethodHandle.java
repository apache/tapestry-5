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

import java.lang.reflect.Method;

/**
 * Similiar to {@link Method}, this allows a method of a Plastic class to be invoked regardless of visibility. Plastic
 * ensures that reflection is not necessary.
 */
public interface MethodHandle
{
    /**
     * Invokes the method for this handle on the instance.
     * 
     * @param instance
     *            the instance containing the method to invoke
     * @param arguments
     *            the arguments, if any, to pass to the method. Wrapper types will be unwrapped as necessary
     *            to perform the invocation.
     * @return result object encapsulating the actual return value or the checked exception thrown by the method
     * @throws ClassCastException
     *             if instance is not the correct type for this method.
     * @throws RuntimeException
     *             if the actual method throws a runtime exception
     */
    MethodInvocationResult invoke(Object instance, Object... arguments);
}
