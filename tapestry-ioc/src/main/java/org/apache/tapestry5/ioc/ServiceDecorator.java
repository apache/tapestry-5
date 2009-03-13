// Copyright 2006, 2009 The Apache Software Foundation
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
 * A service decorator is derived from a {@link org.apache.tapestry5.ioc.def.DecoratorDef} and is responsible for
 * building an interceptor around an existing implementation (called the "delegate").
 *
 * @see org.apache.tapestry5.ioc.ServiceAdvisor
 */
public interface ServiceDecorator
{
    /**
     * Creates a new interceptor object implementing the same service interface as the delegate object.
     *
     * @param delegate an existing object implementing the service interface.
     * @return a new object implementing the same service interface, or delegate or null if the decorator chooses not to
     *         create a new interceptor.
     */
    Object createInterceptor(Object delegate);
}
