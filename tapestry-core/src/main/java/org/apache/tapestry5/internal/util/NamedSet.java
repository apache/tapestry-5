// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import java.util.Set;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Simple, thread-safe associative array that relates a name to a value. Names are case-insensitive.
 * This is optimized to use less memory (than a {@link CaseInsensitiveMap} (it uses a singly-liked list),
 * though the cost of a lookup is more expensive. However, this is a good match against many of the structures inside
 * a page instance, where most lookups occur only during page constructions, and the number of values is often small.
 * <p>
 * We use simply synchronization, as uncontested synchronized locks are very, very cheap.
 * 
 * @param <T>
 *            the type of value stored
 */
public class NamedSet<T>
{
    private NamedRef<T> first;

    private static class NamedRef<T>
    {
        NamedRef<T> next;

        String name;

        T value;

        public NamedRef(String name, T value)
        {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Returns a set of the names of all stored values.
     */
    public synchronized Set<String> getNames()
    {
        Set<String> result = CollectionFactory.newSet();

        NamedRef<T> cursor = first;

        while (cursor != null)
        {
            result.add(cursor.name);
            cursor = cursor.next;
        }

        return result;
    }

    /**
     * Gets the value for the provided name.
     * 
     * @param name
     *            used to locate the value
     * @return the value, or null if not found
     */
    public synchronized T get(String name)
    {
        NamedRef<T> cursor = first;

        while (cursor != null)
        {
            if (cursor.name.equalsIgnoreCase(name)) { return cursor.value; }

            cursor = cursor.next;
        }

        return null;
    }

    /**
     * Stores a new value into the set, replacing any previous value with the same name. Name comparisons are case
     * insensitive.
     * 
     * @param name
     *            to store the value. May not be blank.
     * @param newValue
     *            non-null value to store
     */
    public synchronized void put(String name, T newValue)
    {
        assert InternalUtils.isNonBlank(name);
        assert newValue != null;

        NamedRef<T> prev = null;
        NamedRef<T> cursor = first;

        while (cursor != null)
        {
            if (cursor.name.equalsIgnoreCase(name))
            {
                // Retain the case of the name as put(), even if it doesn't match
                // the existing case

                cursor.name = name;
                cursor.value = newValue;

                return;
            }

            prev = cursor;
            cursor = cursor.next;
        }

        NamedRef<T> newRef = new NamedRef<T>(name, newValue);

        if (prev == null)
            first = newRef;
        else
            prev.next = newRef;
    }
}
