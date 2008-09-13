// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.def.ServiceDef;

/**
 * Allows a module to bind service interfaces to service implementation classes in support of autobuilding services. A
 * ServiceBinder is passed to to a method with the following signature: <code>public static void bind(ServiceBinder
 * binder)</code>. This is an adaptation of ideas from <a href="http://code.google.com/p/google-guice/">Guice</a>.
 */
public interface ServiceBinder
{
    /**
     * Defines a service in terms of an implementation class, without a service interface. In this case, the service
     * will not be proxiable (proxying requires a service interface) and {@link ServiceDef#getServiceInterface()} will
     * return the implementation class. In this situation, the service will not be proxied; it will be instantiated
     * fully on first reference (ignoring its scope, if any) and will not be decorated.
     *
     * @param <T>
     * @param implementationClass class to instantiate as the service
     * @return binding options, used to specify additional details about the service
     */
    <T> ServiceBindingOptions bind(Class<T> implementationClass);

    /**
     * Alternative implementation that supports a callback to build the service, rather than instantiating a particular
     * class.
     *
     * @param serviceInterface interface implemented by the service
     * @param builder          constructs the core service implementation
     * @return binding options, used to specify additional details about the service
     */
    <T> ServiceBindingOptions bind(Class<T> serviceInterface, ServiceBuilder<T> builder);

    /**
     * Binds the service interface to a service implementation class. The default service name is the unqualified name
     * of the service interface. The default service scope is "singleton", unless the service implementation class
     * includes the {@link Scope} annotation.
     *
     * @param <T>
     * @param serviceInterface      service interface (used when locating services, and when building proxies)
     * @param serviceImplementation implementation class that implements the service interface
     * @return binding options, used to specify additional details about the service
     */
    <T> ServiceBindingOptions bind(Class<T> serviceInterface,
                                   Class<? extends T> serviceImplementation);
}
