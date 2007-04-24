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

package org.apache.tapestry.ioc.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeMap;

import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.internal.util.InheritanceSearch;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * A key component in implementing the "Gang of Four" Strategy pattern. A StrategyRegistry will
 * match up a given input type with a registered strategy for that type.
 * 
 * @param <A>
 *            the type of the strategy adapter
 */
public final class StrategyRegistry<A>
{
    private final Class<A> _adapterType;

    private final Map<Class, A> _registrations = newMap();

    private final Map<Class, A> _cache = newThreadSafeMap();

    /**
     * Creates a strategy registry for the given adapter type.
     * 
     * @param adapterType
     *            the type of adapter retrieved from the registry
     * @param registrations
     *            map of registrations (the contents of the map are copied)
     */
    public StrategyRegistry(final Class<A> adapterType, Map<Class, A> registrations)
    {
        _adapterType = adapterType;
        _registrations.putAll(registrations);
    }

    public static <A> StrategyRegistry<A> newInstance(Class<A> adapterType,
            Map<Class, A> registrations)
    {
        return new StrategyRegistry<A>(adapterType, registrations);
    }

    public void clearCache()
    {
        _cache.clear();
    }

    public Class<A> getAdapterType()
    {
        return _adapterType;
    }

    /**
     * Gets an adapter for an object. Searches based on the value's class, unless the value is null,
     * in which case, a search on class void is used.
     * 
     * @param value
     *            for which an adapter is needed
     * @return the adaptoer for the value
     * @throws IllegalArgumentException
     *             if no matching adapter may be found
     */

    public A getByInstance(Object value)
    {
        return get(value == null ? void.class : value.getClass());
    }

    /**
     * Searches for an adapter corresponding to the given input type.
     * 
     * @param type
     *            the type to search
     * @return the corresponding adapter
     * @throws IllegalArgumentException
     *             if no matching adapter may be found
     */
    public A get(Class type)
    {
        A result = _cache.get(type);

        if (result == null)
        {
            result = findMatch(type);
            _cache.put(type, result);
        }

        return result;
    }

    private A findMatch(Class type)
    {
        for (Class t : new InheritanceSearch(type))
        {
            A result = _registrations.get(t);

            if (result != null)
                return result;
        }

        // Report the error. These things really confused the hell out of people in Tap4, so we're
        // going the extra mile on the exception message.

        List<String> names = newList();
        for (Class t : _registrations.keySet())
            names.add(t.getName());

        throw new IllegalArgumentException(UtilMessages.noStrategyAdapter(
                type,
                _adapterType,
                InternalUtils.joinSorted(names)));
    }

    @Override
    public String toString()
    {
        return String.format("StrategyRegistry[%s]", _adapterType.getName());
    }
}
