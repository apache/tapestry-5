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

import org.apache.tapestry5.ioc.def.ServiceDef;

/**
 * Definition of a service advisor, which (by default) is derived from a service advisor method. Service advisor methods
 * are static or instance methods on module classes prefixed with "advise". When a service is realized, a list of
 * matching AdvisorDefs is generated, then ordered, and from each a {@link org.apache.tapestry5.ioc.ServiceAdvisor} is
 * obtained and invoked.
 * <p/>
 * Note: service decorators (via {@link org.apache.tapestry5.ioc.def.DecoratorDef} are applied <em>around</em> the
 * interceptor generated via service advisors, (for compatibility with Tapestry 5.0). In general, you should use service
 * decoration or service advice, not both.
 *
 * @since 5.1.0.0
 */
public interface AdvisorDef
{
    /**
     * Returns the id of the advisor, which is derived from the advisor method name.
     */
    String getAdvisorId();

    /**
     * Returns ordering constraints for this advisor, to order it relative to other advisors.
     *
     * @return zero or more constraint strings
     */
    String[] getConstraints();

    /**
     * Creates an object that can provide the service advice (in the default case, by invoking the advise method on the
     * module class or instance).
     *
     * @param moduleSource used to obtain the module instance
     * @param resources    used to provide injections into the advise method
     * @return advisor
     */
    ServiceAdvisor createAdvisor(ModuleBuilderSource moduleSource, ServiceResources resources);

    /**
     * Used to determine which services may be advised. When advising a service, first the advisors that target the
     * service are identified, then ordering occurs, then the {@link org.apache.tapestry5.ioc.ServiceAdvisor}s are
     * obtained and invoked.
     *
     * @param serviceDef identifies a service that may be advised
     * @return true if this advisor applies to the service
     */
    boolean matches(ServiceDef serviceDef);
}
