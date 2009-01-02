// Copyright 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Scope;

import java.lang.annotation.Annotation;

/**
 * Allows additional options for a service to be specified, overriding hard coded defaults or defaults from annotations
 * on the service.
 *
 * @see org.apache.tapestry5.ioc.def.ServiceDef2
 */
public interface ServiceBindingOptions
{
    /**
     * Allows a specific service id for the service to be provided, rather than the default (from the service
     * interface). This is useful when multiple services implement the same interface, since service ids must be
     * unique.
     *
     * @param id
     * @return this binding options, for further configuration
     */
    ServiceBindingOptions withId(String id);

    /**
     * Sets the scope of the service, overriding the {@link Scope} annotation on the service implementation class.
     *
     * @param scope
     * @return this binding options, for further configuration
     * @see org.apache.tapestry5.ioc.ScopeConstants
     */
    ServiceBindingOptions scope(String scope);

    /**
     * Turns eager loading on for this service. This may also be accomplished using the {@link EagerLoad} annotation on
     * the service implementation class.
     *
     * @return this binding options, for further configuration
     */
    ServiceBindingOptions eagerLoad();

    /**
     * Disallows service decoration for this service.
     *
     * @return this binding options, for further configuration
     */
    ServiceBindingOptions preventDecoration();

    /**
     * Defines the marker interface(s) for the service, used to connect injections by type at the point of injection
     * with a particular service implementation, based on the intersection of type and marker interface. The containing
     * module will sometimes provide a set of default marker annotations for all services within the module, this method
     * allows that default to be extended.
     *
     * @param <T>
     * @param marker one or more markers to add
     * @return this binding options, for further configuration
     */
    <T extends Annotation> ServiceBindingOptions withMarker(Class<T>... marker);
}
