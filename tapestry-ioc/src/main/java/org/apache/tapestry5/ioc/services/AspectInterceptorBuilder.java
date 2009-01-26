// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdviceReceiver;

/**
 * A builder may be obtained from the {@link org.apache.tapestry5.ioc.services.AspectDecorator} and allows more
 * controlled creation of the created interceptor; it allows different methods to be given different advice, and allows
 * methods to be omitted (in which case the method invocation passes through without advice).
 */
public interface AspectInterceptorBuilder<T> extends MethodAdviceReceiver
{

    /**
     * Builds and returns the interceptor.  Any methods that have not been advised will become "pass thrus".
     */
    T build();
}
