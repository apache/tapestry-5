// Copyright 2007, 2008, 2023 The Apache Software Foundation
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
     * Binds the service interface to a conventionally named service implementation class or defines a service in terms of an implementation class, without a service interface.
     * <p>
     * The conventional name for the service implementation class is the same as the name of the service interface with "Impl" appended.
     * For example, {@code bind(Service.class)} will implicitly attempt to bind to {@code ServiceImpl.class}.
     * Use {@link #bind(Class, Class)} if the name of the service implementation class does not follow the convention.
     * <p>
     * In case the service is defined through the implementation class, the service
     * will not be proxiable (proxying requires a service interface) and {@link ServiceDef#getServiceInterface()} will
     * return the implementation class. In this situation, the service will not be proxied; it will be instantiated
     * fully on first reference (ignoring its scope, if any) and will not be decorated.
     *
     * @param <T>
     * @param interfaceClassOrImplementationClass service interface class to bind implicitly or implementation class
     * to instantiate as the service
     * @return binding options, used to specify additional details about the service
     * @see #bind(Class, Class)
     */
    <T> ServiceBindingOptions bind(Class<T> interfaceClassOrImplementationClass);

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
     * <p>
     * The service implementation class may be omitted (in other words, {@link #bind(Class)} used instead) if the name
     * of the service implementation class is the same as the name of the service interface with "Impl" appended.
     * 
     * @param <T>
     * @param serviceInterface      service interface (used when locating services, and when building proxies)
     * @param serviceImplementation implementation class that implements the service interface
     * @return binding options, used to specify additional details about the service
     * @see #bind(Class)
     */
    <T> ServiceBindingOptions bind(Class<T> serviceInterface,
                                   Class<? extends T> serviceImplementation);
}
