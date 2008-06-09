// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.def;

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;

/**
 * Definition of a service decorator, which (by default) is derived from a service decorator method.
 * <p/>
 * A note on decorator scheduling. The scheduling is based on the desired order of <em>behavior</em>. Thus, if logging
 * should occur before security checks, and security checks should occur before transaction management, then the desired
 * decorator order is Logging, Security, Transactions. This might be specified as having Security occur after Logging,
 * and Transactions occur after Security. It might also be specified by having Logging ordered "before:*", and
 * Transactions ordered "after:*" with no specified scheduling for Security.
 * <p/>
 * Once this order is established, decorators are <em>applied</em> in reverse order. Each decorator's job is to create
 * an <em>interceptor</em> for the service, that delegates to the next implementation. This implies that the decorators
 * are executed last to first. In the above example, the core service implementation would be passed to the Transaction
 * decorator, resulting in the Transaction interceptor. The Transaction interceptor would be passed to the Security
 * decorator, resulting in the Security interceptor. The Security interceptor would be passed to the Logging decorator,
 * resulting in the Logging interceptor. Thus at runtime, the Logging interceptor will execute first, then delegate to
 * the Security interceptor, which would delegate to the Transaction interceptor, which would finally delegate to the
 * core service implementation.
 */
public interface DecoratorDef
{
    /**
     * Returns the id of the decorator, which is derived from the decorator method name.
     */
    String getDecoratorId();

    /**
     * Returns zero or more ordering constraint strings, used to order the decorated relative to the other decorators.
     */

    String[] getConstraints();

    /**
     * Creates an object that can perform the decoration (in the default case, by invoking the decorator method on the
     * module builder instance.
     *
     * @param moduleBuilderSource the module builder instance associated with the module containing the decorator (not
     *                            necessarily the module containing the service being decorated)
     * @param resources           the resources visible <em>to the decorator</em> (which may be in a different module
     *                            than the service being decorated). Other resource properties (serviceId,
     *                            serviceInterface, log, etc.) are for the service being decorated.
     */
    ServiceDecorator createDecorator(ModuleBuilderSource moduleBuilderSource,
                                     ServiceResources resources);

    /**
     * Used to determine which services may be decorated by this decorator. When decorating a service, first the
     * decorators that target the service are identified, then ordering occurs, then the {@link ServiceDecorator}s are
     * invoked.
     *
     * @param serviceDef
     * @return true if the decorator applies to the service
     */
    boolean matches(ServiceDef serviceDef);
}
