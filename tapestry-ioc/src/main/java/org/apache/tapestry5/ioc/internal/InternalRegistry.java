// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Internal view of the module registry, adding additional methods needed by modules.
 */
public interface InternalRegistry extends Registry, RegistryShutdownHub, OperationTracker
{
    /**
     * As with {@link org.apache.tapestry5.ioc.Registry#getObject(Class, org.apache.tapestry5.commons.AnnotationProvider)},
     * but handles the {@link org.apache.tapestry5.ioc.annotations.Local} annotation.
     *
     * @param objectType         type of object o be injected
     * @param annotationProvider access to annotations at point of injection
     * @param locator            used to resolve any subsequent injections
     * @param localModule        module to limit services to, if Local annotaton present
     * @return the service or object
     */
    <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator,
                    Module localModule);

    /**
     * Returns a service lifecycle by service scope name.
     *
     * @param scope the name of the service scope (case insensitive)
     * @return the lifecycle corresponding to the scope
     * @throws RuntimeException if the lifecycle name does not match a known lifecycle
     */
    ServiceLifecycle2 getServiceLifecycle(String scope);

    /**
     * Searches for decorators for a particular service. The resulting {@link org.apache.tapestry5.ioc.def.DecoratorDef}
     * s
     * are ordered, then converted into {@link ServiceDecorator}s.
     */
    List<ServiceDecorator> findDecoratorsForService(ServiceDef3 serviceDef);

    /**
     * Searches for advisors for a particular service, returning them in order of application.
     *
     * @since 5.1.0.0
     */
    List<ServiceAdvisor> findAdvisorsForService(ServiceDef3 serviceDef);

    /**
     * Builds up an unordered collection by invoking service contributor methods that target the service (from any
     * module, unless the service is private).
     *
     * @param <T>
     * @param serviceDef defines the service for which configuration data is being assembled
     * @param valueType  identifies the type of object allowed into the collection
     * @return the final collection
     */
    <T> Collection<T> getUnorderedConfiguration(ServiceDef3 serviceDef, Class<T> valueType);

    /**
     * Builds up an ordered collection by invoking service contributor methods that target the service (from any module,
     * unless the service is private). Once all values have been added (each with an id, and pre/post constraints), the
     * values are ordered, null values dropped, and the final sorted list is returned.
     *
     * @param <T>
     * @param serviceDef defines the service for which configuration data is being assembled
     * @param valueType  identifies the type of object allowed into the collection
     * @return the final ordered list
     */
    <T> List<T> getOrderedConfiguration(ServiceDef3 serviceDef, Class<T> valueType);

    /**
     * Builds up a map of key/value pairs by invoking service contribution methods that target the service (from any
     * module, unless the service is private). Values and keys may not be null. Invalid values (keys or values that are
     * the wrong type, or duplicate keys) result in warnings and are ignored.
     *
     * @param <K>
     * @param <V>
     * @param serviceDef defines the service for which configuration data is being assembled
     * @param keyType    identifies the type of key object allowed into the map
     * @param valueType  identifies the type of value object allowed into the map
     * @return the final ordered list
     */
    <K, V> Map<K, V> getMappedConfiguration(ServiceDef3 serviceDef, Class<K> keyType, Class<V> valueType);


    /**
     * Given an input string that <em>may</em> contain symbols, returns the string with any and all symbols fully
     * expanded.
     *
     * @param input
     * @return expanded input
     */
    String expandSymbols(String input);

    /**
     * Returns a logger for the service, which consists of the Module's {@link Module#getLoggerName() log name} suffixed
     * with a period and the service id.
     *
     * @param serviceId
     * @return the logger for the service
     */
    Logger getServiceLogger(String serviceId);

    /**
     * Creates a just-in-time (and possibly, live reloading) proxy for the indicated class and interface, using the
     * provided locator to autobuild the implementationClass (when necessary).
     *
     * @since 5.2.0
     */
    <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass, ObjectLocator locator);

    /**
     * Returns a Set of Annotation classes that are used as service markers.
     *
     * @since 5.2.0
     */
    Set<Class> getMarkerAnnotations();
}
