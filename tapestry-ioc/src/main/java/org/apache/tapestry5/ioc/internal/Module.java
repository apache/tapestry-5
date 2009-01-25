// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.AdvisorDef;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef2;

import java.util.Collection;
import java.util.Set;

/**
 * A module within the Tapestry IoC registry. Each Module is constructed around a corresponding module builder instance;
 * the methods and annotations of that instance define the services provided by the module.
 */
public interface Module extends ModuleBuilderSource
{
    /**
     * Locates a service given a service id and the corresponding service interface type.
     *
     * @param <T>
     * @param serviceId        identifies the service to access
     * @param serviceInterface the interface the service implements
     * @return the service's proxy
     * @throws RuntimeException if there is an error instantiating the service proxy
     */
    <T> T getService(String serviceId, Class<T> serviceInterface);

    /**
     * Locates the ids of all services that implement the provided service interface, or whose service interface is
     * assignable to the provided service interface (is a super-class or super-interface).
     *
     * @param serviceInterface the interface to search for
     * @return a collection of service ids
     */
    Collection<String> findServiceIdsForInterface(Class serviceInterface);

    /**
     * Iterates over any decorator definitions defined by the module and returns those that apply to the provided
     * service definition.
     *
     * @param serviceDef for which decorators are being assembled
     * @return set of decorators, possibly empty (but not null)
     */
    Set<DecoratorDef> findMatchingDecoratorDefs(ServiceDef serviceDef);

    /**
     * Iterates over any advisor definitions defined by the module and returns those that apply to the provided service
     * definition.
     *
     * @param serviceDef for which advisors are being assembled
     * @return set of advisors, possibly empty but not null
     * @since 5.1.0.0
     */
    Set<AdvisorDef> findMatchingServiceAdvisors(ServiceDef serviceDef);

    /**
     * Finds any contributions that are targetted at the indicated service.
     */
    Set<ContributionDef> getContributorDefsForService(String serviceId);

    /**
     * Locates services with the {@link org.apache.tapestry5.ioc.annotations.EagerLoad} annotation and generates proxies
     * for them, then adds them to the proxies list for instantiation.
     *
     * @param proxies collection of proxies to which any eager load services in the module should be added
     */
    void collectEagerLoadServices(Collection<EagerLoadServiceProxy> proxies);

    /**
     * Returns the service definition for the given service id.
     *
     * @param serviceId unique id for the service (caseless)
     * @return the service definition or null
     */
    ServiceDef2 getServiceDef(String serviceId);

    /**
     * Returns the name used to obtain a logger for the module. Services within the module suffix this with a period and
     * the service id.
     *
     * @return module logger name
     */
    String getLoggerName();
}
