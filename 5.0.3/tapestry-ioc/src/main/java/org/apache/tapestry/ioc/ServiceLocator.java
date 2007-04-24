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

package org.apache.tapestry.ioc;

/**
 * Defines an object which can provide access to services defined within a
 * {@link org.apache.tapestry.ioc.Registry}. Services are accessed via service id, or (when
 * appropriate) by just service interface. The Registry itself implements this interface, as does
 * {@link org.apache.tapestry.ioc.ServiceResources}.
 */
public interface ServiceLocator
{

    /**
     * Obtains a service via its unique service id. Returns the service's proxy. The service proxy
     * implements the same interface as the actual service, and is used to instantiate the actual
     * service only as needed (this is transparent to the application).
     * 
     * @param <T>
     * @param serviceId
     *            unique ervice id used to locate the service object (may contain <em>symbols</em>,
     *            which will be expanded)
     * @param serviceInterface
     *            the interface implemented by the service (or an interface extended by the service
     *            interface)
     * @return the service instance
     * @throws RuntimeException
     *             if the service is not defined, or if an error occurs instantitating it
     */
    <T> T getService(String serviceId, Class<T> serviceInterface);

    /**
     * Locates a service given just a service interface. A single service must implement the service
     * interface (which can be hard to guarantee).
     * 
     * @param <T>
     * @param serviceInterface
     *            the interface the service implements
     * @return the service's proxy
     * @throws RuntimeException
     *             if the service does not exist (this is considered programmer error), or multiple
     *             services implement the service interface
     */
    <T> T getService(Class<T> serviceInterface);

    /**
     * Obtains an object indirectly, using an {@link ObjectProvider} identified by the prefix of the
     * reference.
     * 
     * @param <T>
     * @param reference
     *            a provider prefix, a colon, an expression meaningful to the provider (may contain
     *            <em>symbols</em>, which will be expanded)
     * @param objectType
     *            the type of object to be returned
     * @return the requested object
     * @see ObjectProvider
     */
    <T> T getObject(String reference, Class<T> objectType);

}