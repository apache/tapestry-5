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

package org.apache.tapestry5.ioc.services;

import java.util.List;

/**
 * Organizes all {@link org.apache.tapestry5.ioc.services.PropertyAdapter}s for a particular class.
 * <p/>
 * Only provides access to <em>simple</em> properties. Indexed properties are ignored.
 * <p/>
 * When accessing properties by name, the case of the name is ignored.
 */
public interface ClassPropertyAdapter
{
    /**
     * Returns the names of all properties, sorted into alphabetic order.
     */
    List<String> getPropertyNames();

    /**
     * Returns the type of bean this adapter provides properties for.
     */
    Class getBeanType();

    /**
     * Returns the property adapter with the given name, or null if no such adapter exists.
     *
     * @param name of the property (case is ignored)
     */
    PropertyAdapter getPropertyAdapter(String name);

    /**
     * Reads the value of a property.
     *
     * @param instance     the object to read a value from
     * @param propertyName the name of the property to read (case is ignored)
     * @throws UnsupportedOperationException if the property is write only
     * @throws IllegalArgumentException      if property does not exist
     */
    Object get(Object instance, String propertyName);

    /**
     * Updates the value of a property.
     *
     * @param instance     the object to update
     * @param propertyName the name of the property to update (case is ignored)
     * @throws UnsupportedOperationException if the property is read only
     * @throws IllegalArgumentException      if property does not exist
     */
    void set(Object instance, String propertyName, Object value);
}
