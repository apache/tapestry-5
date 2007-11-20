// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


/**
 * Used to "uniquify" names within a given context. A base name is passed in, and the return value
 * is the base name, or the base name extended with a suffix to make it unique.
 * <p/>
 * This class is not threadsafe.
 */

public final class IdAllocator
{
    private static final String SEPARATOR = "_";

    /**
     * Map from allocated id to a generator for names associated with the allocated id.
     */
    private final Map<String, NameGenerator> _generatorMap;

    private final String _namespace;

    /**
     * Generates unique names with a particular prefix.
     */
    private static class NameGenerator implements Cloneable
    {
        private final String _baseId;

        private int _index;

        NameGenerator(String baseId)
        {
            _baseId = baseId + SEPARATOR;
        }

        public String nextId()
        {
            return _baseId + _index++;
        }

        /**
         * Clones this instance, returning an equivalent but seperate copy.
         */
        @Override
        public NameGenerator clone()
        {
            try
            {
                return (NameGenerator) super.clone();
            }
            catch (CloneNotSupportedException ex)
            {
                // Unreachable!
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Creates a new allocator with no namespace.
     */
    public IdAllocator()
    {
        this("");
    }

    /**
     * Creates a new allocator with the provided namespace.
     */
    public IdAllocator(String namespace)
    {
        this(namespace, new HashMap<String, NameGenerator>());
    }

    private IdAllocator(String namespace, Map<String, NameGenerator> generatorMap)
    {
        _namespace = namespace;
        _generatorMap = generatorMap;
    }

    /**
     * Returns a list of all allocated ids, sorted alphabetically.
     */
    public List<String> getAllocatedIds()
    {
        return InternalUtils.sortedKeys(_generatorMap);
    }

    /**
     * Creates a clone of this IdAllocator instance, copying the allocator's namespace and key map.
     */
    @Override
    public IdAllocator clone()
    {
        // Copying the _generatorMap is tricky; multiple keys will point to the same NameGenerator
        // instance. We need to clone the NameGenerators, then build a new map around the clones.

        IdentityHashMap<NameGenerator, NameGenerator> transformMap = new IdentityHashMap<NameGenerator, NameGenerator>();

        for (NameGenerator original : _generatorMap.values())
        {
            NameGenerator copy = original.clone();

            transformMap.put(original, copy);
        }

        Map<String, NameGenerator> mapCopy = newMap();

        for (String key : _generatorMap.keySet())
        {
            NameGenerator original = _generatorMap.get(key);
            NameGenerator copy = transformMap.get(original);

            mapCopy.put(key, copy);
        }

        return new IdAllocator(_namespace, mapCopy);
    }

    /**
     * Allocates the id. Repeated calls for the same name will return "name", "name_0", "name_1",
     * etc.
     */

    public String allocateId(String name)
    {
        String key = name + _namespace;

        NameGenerator g = _generatorMap.get(key);
        String result = null;

        if (g == null)
        {
            g = new NameGenerator(key);
            result = key;
        }
        else result = g.nextId();

        // Handle the degenerate case, where a base name of the form "foo_0" has been
        // requested. Skip over any duplicates thus formed.

        while (_generatorMap.containsKey(result)) result = g.nextId();

        _generatorMap.put(result, g);

        return result;
    }

    /**
     * Checks to see if a given name has been allocated.
     */
    public boolean isAllocated(String name)
    {
        return _generatorMap.containsKey(name);
    }

    /**
     * Clears the allocator, resetting it to freshly allocated state.
     */

    public void clear()
    {
        _generatorMap.clear();
    }
}