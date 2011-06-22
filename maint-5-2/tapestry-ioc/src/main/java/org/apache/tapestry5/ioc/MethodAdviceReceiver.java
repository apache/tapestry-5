// Copyright 2009 The Apache Software Foundation
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

import java.lang.reflect.Method;

/**
 * Interface used with service advisor methods to define advice.  Allows advice on specific methods, or on all methods.
 */
public interface MethodAdviceReceiver
{
    /**
     * Adds advice for a specific method of the aspect interceptor being constructed.
     *
     * @param method method (of the interface for which an interceptor is being constructed) to be advised. Multiple
     *               advice is allowed for a single method; the advice will be executed in the order it is added.
     * @param advice the advice for this particular method.   Advice must be threadsafe.
     */
    void adviseMethod(Method method, MethodAdvice advice);

    /**
     * Advises <em>all</em> methods of the interface with the given advice.
     */
    void adviseAllMethods(MethodAdvice advice);

    /**
     * Returns the interface for which methods may be advised.
     *
     * @see org.apache.tapestry5.ioc.services.MethodIterator
     * @since 5.1.0.0
     */
    Class getInterface();
}
