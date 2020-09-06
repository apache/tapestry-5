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

package org.apache.tapestry5.commons;

/**
 * Object passed into a service contributor method that allows the method provide contributed values to the service's
 * configuration.
 *
 * A service can <em>collect</em> contributions in three different ways:
 * <ul>
 * <li>As an un-ordered collection of values</li>
 * <li>As an ordered list of values (where each value has a unique id, pre-requisites and post-requisites)</li>
 * <li>As a map of keys and values
 * </ul>
 *
 * The service defines the <em>type</em> of contribution, in terms of a base class or service interface. Contributions
 * must be compatible with the type.
 */
public interface MappedConfiguration<K, V>
{

    /**
     * Adds a keyed object to the service's contribution.
     * 
     * @param key
     *            unique id for the value
     * @param value
     *            to contribute
     * @throws IllegalArgumentException
     *             if key is not unique
     */
    void add(K key, V value);

    /**
     * Overrides an existing contribution by its key.
     * 
     * @param key
     *            unique id of value to override
     * @param value
     *            new value, or null to remove the key entirely
     * @since 5.1.0.0
     */
    void override(K key, V value);

    /**
     * Adds a keyed object as an instantiated instance (with dependencies injected) of a class. When the value
     * type is an interface and the class to be contributed is a local file,
     * then a reloadable proxy for the value class will be created and contributed.
     * 
     * @param key
     *            unique id for the value
     * @param clazz
     *            class to instantiate and contribute
     * @since 5.1.0.0
     */
    void addInstance(K key, Class<? extends V> clazz);

    /**
     * Overrides an existing contribution with a new instance. When the value
     * type is an interface and the class to be contributed is a local file,
     * then a reloadable proxy for the value class will be created and contributed.
     * 
     * @param key
     *            unique id of value to override
     * @param clazz
     *            class to instantiate as override
     */
    void overrideInstance(K key, Class<? extends V> clazz);
}
