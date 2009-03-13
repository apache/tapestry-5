// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

/**
 * Object passed into a service contributor method that allows the method provide contributed values to the service's
 * configuration.
 * <p/>
 * A service can <em>collect</em> contributions in three different ways: <ul> <li>As an un-ordered collection of
 * values</li> <li>As an ordered list of values (where each value has a unique id, pre-requisites and
 * post-requisites)</li> <li>As a map of keys and values </ul>
 * <p/>
 * The service defines the <em>type</em> of contribution, in terms of a base class or service interface. Contributions
 * must be compatible with the type.
 */
public interface OrderedConfiguration<T>
{
    /**
     * Adds an ordered object to a service's contribution. Each object has an id (which must be unique). Optionally,
     * pre-requisites (a list of ids that must precede this object) and post-requisites (ids that must follow) can be
     * provided.
     *
     * @param id          a unique id for the object; the id will be fully qualified with the contributing module's id
     * @param constraints used to order the object relative to other contributed objects
     * @parm object to add to the service's configuration
     */
    void add(String id, T object, String... constraints);

    /**
     * Overrides a normally contributed object.  Each override must match a single normally contributed object.
     *
     * @param id          identifies object to override
     * @param object      overriding object (may be null)
     * @param constraints contrains for the overridden object, replacing constraints for the original object (even if
     *                    omitted, in which case the override object will have no orderring contraints)
     * @since 5.1.0.0
     */
    void override(String id, T object, String... constraints);

    /**
     * Adds an ordered object by instantiating (with dependencies) the indicated class.
     *
     * @param id          of contribution (used for ordering)
     * @param clazz       class to instantiate
     * @param constraints used to order the object relative to other contributed objects
     * @since 5.1.0.0
     */
    void addInstance(String id, Class<? extends T> clazz, String... constraints);

    /**
     * Instantiates an object and adds it as an override.
     *
     * @param id          of object to override
     * @param clazz       to instantiate
     * @param constraints override contraints
     * @since 5.1.0.0
     */
    void overrideInstance(String id, Class<? extends T> clazz, String... constraints);
}
