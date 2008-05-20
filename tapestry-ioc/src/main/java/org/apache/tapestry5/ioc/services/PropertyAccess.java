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

package org.apache.tapestry5.ioc.services;

/**
 * A wrapper around the JavaBean Introspector that allows more manageable access to JavaBean properties of objects.
 * <p/>
 * <p/>
 * Only provides access to <em>simple</em> properties.  Indexed properties are ignored.
 */
public interface PropertyAccess
{
    /**
     * Reads the value of a property.
     *
     * @throws UnsupportedOperationException if the property is write only
     * @throws IllegalArgumentException      if property does not exist
     */
    Object get(Object instance, String propertyName);

    /**
     * Updates the value of a property.
     *
     * @throws UnsupportedOperationException if the property is read only
     * @throws IllegalArgumentException      if property does not exist
     */
    void set(Object instance, String propertyName, Object value);

    /**
     * Returns the adapter for a particular object instance. A convienience over invoking {@link #getAdapter(Class)}.
     */
    ClassPropertyAdapter getAdapter(Object instance);

    /**
     * Returns the adapter used to access properties within the indicated class.
     */
    ClassPropertyAdapter getAdapter(Class forClass);

    /**
     * Discards all stored property access information, discarding all created class adapters.
     */
    void clearCache();
}
