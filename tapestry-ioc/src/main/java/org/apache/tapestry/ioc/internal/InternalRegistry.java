// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.LogSource;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceLifecycle;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.RegistryShutdownHub;

/**
 * Internal view of the module registry, adding additional methods needed by modules.
 */
public interface InternalRegistry extends Registry, LogSource, RegistryShutdownHub
{
    /**
     * Locates a service given a fully qualified service id and the corresponding service interface
     * type. This is typically invoked by a module (passing itself as the third parameter), but may
     * also be invoked by the registry itself, passing null for module.
     * 
     * @param <T>
     * @param serviceId
     *            the simple or fully qualified service id
     * @param serviceInterface
     *            the interface the service implements
     * @param module
     *            the module requesting the service (used for visibility checks)
     * @return the service's proxy
     * @throws RuntimeException
     *             if the service does not exist (this is considered programmer error)
     */
    <T> T getService(String serviceId, Class<T> serviceInterface, Module module);

    /**
     * Locates a service given just a service interface. A single service must implement the service
     * interface (which can be hard to guarantee). This is typically invoked by a module (passing
     * itself as the third parameter), but may also be invoked by the registry itself, passing null
     * for module.
     * 
     * @param <T>
     * @param serviceInterface
     *            the interface the service implements
     * @param module
     *            the module requesting the service (used for visibility checks)
     * @return the service's proxy
     * @throws RuntimeException
     *             if the service does not exist (this is considered programmer error), or multiple
     *             services implement the service interface
     */
    <T> T getService(Class<T> serviceInterface, Module module);

    /**
     * Returns a service lifecycle by name.
     * 
     * @param lifecycle
     *            the name of the lifecycle
     * @return the lifecycle instance
     * @throws RuntimeException
     *             if the lifecycle name does not match a known lifecycle
     */
    ServiceLifecycle getServiceLifecycle(String lifecycle);

    /**
     * Searches for decorators for a particular service. The resulting
     * {@link org.apache.tapestry.ioc.def.DecoratorDef}s are ordered, then converted into
     * {@link ServiceDecorator}s.
     */
    List<ServiceDecorator> findDecoratorsForService(ServiceDef serviceDef);

    /**
     * Builds up an unordered collection by invoking service contributor methods that target the
     * service (from any module, unless the service is private).
     * 
     * @param <T>
     * @param serviceDef
     *            defines the service for which configuration data is being assembled
     * @param valueType
     *            identifies the type of object allowed into the collection
     * @return the final collection
     */
    <T> Collection<T> getUnorderedConfiguration(ServiceDef serviceDef, Class<T> valueType);

    /**
     * Builds up an ordered collection by invoking service contributor methods that target the
     * service (from any module, unless the service is private). Once all values have been added
     * (each with an id, and pre/post constraints), the values are ordered, null values dropped, and
     * the final sorted list is returned.
     * 
     * @param <T>
     * @param serviceDef
     *            defines the service for which configuration data is being assembled
     * @param valueType
     *            identifies the type of object allowed into the collection
     * @return the final ordered list
     */
    <T> List<T> getOrderedConfiguration(ServiceDef serviceDef, Class<T> valueType);

    /**
     * Builds up a map of key/value pairs by invoking service contribution methods that tharget the
     * service (from any module, unless the service is private). Values and keys may not be null.
     * Invalid values (keys or values that are the wrong type, or duplicate keys) result in warnings
     * and are ignored.
     * 
     * @param <K,
     *            V>
     * @param serviceDef
     *            defines the service for which configuration data is being assembled
     * @param keyType
     *            identifies the type of key object allowed into the map
     * @param valueType
     *            identifies the type of value object allowed into the map
     * @return the final ordered list
     */
    <K, V> Map<K, V> getMappedConfiguration(ServiceDef serviceDef, Class<K> keyType,
            Class<V> valueType);

    /**
     * Convieience for creating a new {@link ClassFab} instance using a
     * {@link org.apache.tapestry.ioc.services.ClassFactory}.
     * 
     * @param serviceInterface
     *            the interface to be implemented by the provided class
     */
    ClassFab newClass(Class serviceInterface);

    /**
     * Provides an object by delegating to the tapestry.ioc.MasterObjectProvider service.
     * 
     * @param reference
     *            A string used to identify the objecty to be provided
     * @param objectType
     *            the expected type of object
     * @param locator
     *            identifies what services are visible in the context
     * @see ObjectProvider#provide(String, Class, ServiceLocator)
     */
    <T> T getObject(String reference, Class<T> objectType, ServiceLocator locator);

    /**
     * Given an input string that <em>may</em> contain symbols, returns the string with any and
     * all symbols fully expanded.
     * 
     * @param input
     * @return expanded input
     */
    String expandSymbols(String input);
}
