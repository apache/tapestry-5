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

package org.apache.tapestry.ioc.def;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;

/**
 * Service definition derived, by default, from a service builder method.
 */
public interface ServiceDef
{
    /**
     * Returns an {@link ObjectCreator} that can create the core service implementation.
     * 
     * @param resources
     *            used to resolve dependencies of the service, or access its configuration
     * @return an object that can (later) be used to instantiate the service itself
     */
    ObjectCreator createServiceCreator(ServiceBuilderResources resources);

    /** Returns the service id, derived from the method name. */
    String getServiceId();

    /**
     * Returns the service interface associated with this service. This is the interface exposed to
     * the outside world, as well as the one used to build proxies. In cases where the service is
     * <em>not</em> defined in terms of an interface, this will return the actual implementation
     * class of the service. Services without a true service interfaced are <strong>not proxied</strong>.
     */
    Class getServiceInterface();

    /**
     * Returns the lifecycle defined for the service. This is indicated by adding a
     * {@link org.apache.tapestry.ioc.annotations.Scope} annotation to the service builder method
     * for the service.
     * <p>
     * Services that are not proxied will ignore thier scope; such services are always treated as
     * singletons.
     */
    String getServiceScope();

    /**
     * Returns true if the service should be eagerly loaded at Registry startup.
     * 
     * @see org.apache.tapestry.ioc.annotations.EagerLoad
     */
    boolean isEagerLoad();
}