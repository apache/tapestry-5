// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.internal.util.Defense;

/**
 * A default, extensible version of {@link PrimaryKeyEncoder} that is based on loading known values
 * into an internal map. When there's a reasonable number (hundreds, perhaps thousands) of items to
 * choose from, and those items are fast and cheap to read and instantiate, this implementation is a
 * good bet. For very large result sets, you'll need to create your own implementation of
 * {@link PrimaryKeyEncoder}.
 * 
 * @param <K>
 *            the key type (which must be serializable)
 * @param <V>
 *            the value type
 */
public class DefaultPrimaryKeyEncoder<K extends Serializable, V> implements PrimaryKeyEncoder<K, V>
{
    private final Map<K, V> _keyToValue = new LinkedHashMap<K, V>();

    private final Map<V, K> _valueToKey = newMap();

    private Set<K> _deletedKeys;

    private K _currentKey;

    /** Adds a new key/value pair to the encoder. */
    public final void add(K key, V value)
    {
        Defense.notNull(key, "key");
        Defense.notNull(value, "value");

        V existing = _keyToValue.get(key);
        if (existing != null)
            throw new IllegalArgumentException(TapestryMessages.duplicateKey(key, value, existing));

        _keyToValue.put(key, value);

        // TODO: Ensure that the value is unique?

        _valueToKey.put(value, key);
    }

    /**
     * Returns the values previously {@link #add(Serializable, Object) added to the encoder},
     * <em>in the order in which they were added</em>. Values that are deleted are not returned.
     * 
     * @return ordered list of values
     */
    public final List<V> getValues()
    {
        return valuesNotInKeySet(_deletedKeys);
    }

    /**
     * Returns a list of all the values <em>except</em> those values whose keys are in the
     * provided set. The set may be null, in which case all values are returned.
     * 
     * @param keySet
     *            set of keys identifying values to exclude, or null to exclude no values
     * @return values (not in the set) in order origionally added
     */
    protected final List<V> valuesNotInKeySet(Set<K> keySet)
    {
        if (keySet == null || keySet.isEmpty())
            return getAllValues();

        List<V> result = newList();

        for (Map.Entry<K, V> entry : _keyToValue.entrySet())
        {

            if (keySet.contains(entry.getKey()))
                continue;

            result.add(entry.getValue());
        }

        return result;
    }

    public final List<V> getAllValues()
    {
        List<V> result = newList();

        for (Map.Entry<K, V> entry : _keyToValue.entrySet())
        {
            result.add(entry.getValue());
        }

        return result;
    }

    /**
     * For a previously {@link #add(Serializable, Object) added key/value pair}, returns the key
     * corresponding to the given value.
     */
    public final K toKey(V value)
    {
        Defense.notNull(value, "value");

        _currentKey = _valueToKey.get(value);

        if (_currentKey == null)
            throw new IllegalArgumentException(TapestryMessages.missingValue(value, _valueToKey
                    .keySet()));

        return _currentKey;
    }

    public final V toValue(K key)
    {
        V result = _keyToValue.get(key);

        if (result == null)
        {
            result = provideMissingObject(key);

            _currentKey = key;
        }
        else
        {
            _currentKey = key;
        }

        return result;
    }

    /**
     * Invoked by {@link #toValue(Serializable)} whenever a key can not be converted to a value
     * using the internal cache. This is an opportunity to record the fact that an error occured
     * (they key was not valuable, possibly because it points to a deleted entity object) and
     * provide a temporary object. This method may return null, but in a typical application, that
     * will likely case NullPointerExceptions further down the processing chain.
     * <p>
     * This implementation returns null, and is intended to be overriden in subclasses.
     * 
     * @param key
     *            key for which a value is required
     * @return a substitute value, or null
     */
    protected V provideMissingObject(K key)
    {
        return null;
    }

    public final boolean isDeleted()
    {
        return inKeySet(_deletedKeys);
    }

    public final void setDeleted(boolean value)
    {
        _deletedKeys = modifyKeySet(_deletedKeys, value);
    }

    /**
     * Returns true if the current key is in the provided set.
     * 
     * @param keySet
     *            the set of keys to check, or null
     * @return true if the key is in the set, false if it is missing (or if keySet is null)
     */
    protected final boolean inKeySet(Set<K> keySet)
    {
        return keySet != null ? keySet.contains(_currentKey) : false;
    }

    /**
     * Modifies a keySet to add or remove the current key. If necessary, a new Set is created.
     * <p>
     * Useage: <code>
     * private Set<K> _myFlagKeys;
     * 
     * public boolean void setMyFlag(boolean value)
     * {
     *   _myFlagKeys = modifySet(_myFlagKeys, value);
     * }
     * </code>
     * 
     * @param keySet
     *            the set of keys, or null
     * @param value
     *            true to add the current key, false to remove
     * @return the provided key set, or a new one
     */
    protected final Set<K> modifyKeySet(Set<K> keySet, boolean value)
    {
        if (keySet == null)
        {
            if (!value)
                return null;

            keySet = newSet();
        }

        if (value)
            keySet.add(_currentKey);
        else
            keySet.remove(_currentKey);

        return keySet;
    }

    /** Does nothing. Subclasses may override as necessary. */
    public void prepareForKeys(List<K> keys)
    {
    }
}
