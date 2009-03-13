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
 * This implementation is used for un-ordered configuration data.
 * <p/>
 * The service defines the <em>type</em> of contribution, in terms of a base class or service interface. Contributions
 * must be compatible with the type.
 */
public interface Configuration<T>
{
    /**
     * Adds an object to the service's contribution.
     *
     * @param object to add to the service's configuration
     */
    void add(T object);

    /**
     * Automatically instantiates an instance of the class, with dependencies injeted, and adds it to the
     * configuration.
     *
     * @param clazz what class to instantiate
     * @since 5.1.0.0
     */
    void addInstance(Class<? extends T> clazz);
}
