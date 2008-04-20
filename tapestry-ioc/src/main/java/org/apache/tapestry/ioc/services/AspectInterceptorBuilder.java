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

package org.apache.tapestry.ioc.services;

import org.apache.tapestry.ioc.MethodAdvice;

import java.lang.reflect.Method;

/**
 * A builder may be obtained from the {@link org.apache.tapestry.ioc.services.AspectDecorator} and allows more
 * controlled creation of the created interceptor; it allows different methods to be given different advice, and allows
 * methods to be omitted (in which case the method invocation passes through without advice).
 */
public interface AspectInterceptorBuilder<T>
{
    /**
     * Adds advice for a specific method of the aspect interceptor being constructed.
     *
     * @param method method (of the interface for which an interceptor is being constructed) to be adviced. A method may
     *               only be advised <em>once</em> (for a single interceptor; it is valid to chain together a series of
     *               interceptors).
     * @param advice the advice for this particular method.   Advice must be threadsafe.
     */
    void adviseMethod(Method method, MethodAdvice advice);

    /**
     * Builds and returns the interceptor.  Any methods that have not been advised will become "pass thrus".
     */
    T build();
}
