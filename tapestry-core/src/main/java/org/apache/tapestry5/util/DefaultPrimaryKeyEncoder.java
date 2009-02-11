// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import org.apache.tapestry5.PrimaryKeyEncoder;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A default, extensible version of {@link org.apache.tapestry5.PrimaryKeyEncoder} that is based on loading known values
 * into an internal map. When there's a reasonable number (hundreds, perhaps thousands) of items to choose from, and
 * those items are fast and cheap to read and instantiate, this implementation is a good bet. For very large result
 * sets, you'll need to create your own implementation of {@link PrimaryKeyEncoder}.
 *
 * @param <K> the key type (which must be serializable)
 * @param <V> the value type
 * @deprecated See deprecation notes for {@link org.apache.tapestry5.PrimaryKeyEncoder}.
 */
public class DefaultPrimaryKeyEncoder<K extends Serializable, V> implements PrimaryKeyEncoder<K, V>
{
    private final Map<K, V> keyToValue = new LinkedHashMap<K, V>();

    private final Map<V, K> valueToKey = CollectionFactory.newMap();

    private Set<K> deletedKeys;

    private K currentKey;

    private final Class<K> keyType;

    /**
     * Compatibility with 5.0: new encoder, key type unknown. You <em>will</em> want to use the other constructor and
     * specify the key type.
     */
    public DefaultPrimaryKeyEncoder()
    {
        this(null);
    }

    /**
     * @since 5.1.0.0
     */
    public DefaultPrimaryKeyEncoder(Class<K> keyType)
    {
        this.keyType = keyType;
    }


    public Class<K> getKeyType()
    {
        return keyType;
    }

    /**
     * Adds a new key/value pair to the encoder.
     */
    public final void add(K key, V value)
    {
        Defense.notNull(key, "key");
        Defense.notNull(value, "value");

        V existing = keyToValue.get(key);
        if (existing != null) throw new IllegalArgumentException(PublicUtilMessages.duplicateKey(key, value, existing));

        keyToValue.put(key, value);

        // TODO: Ensure that the value is unique?

        valueToKey.put(value, key);
    }

    /**
     * Returns the values previously {@link #add(Serializable, Object) added to the encoder}, <em>in the order in which
     * they were added</em>. Values that are deleted are not returned.
     *
     * @return ordered list of values
     */
    public final List<V> getValues()
    {
        return valuesNotInKeySet(deletedKeys);
    }

    /**
     * Returns a list of all the values <em>except</em> those values whose keys are in the provided set. The set may be
     * null, in which case all values are returned.
     *
     * @param keySet set of keys identifying values to exclude, or null to exclude no values
     * @return values (not in the set) in order origionally added
     */
    protected final List<V> valuesNotInKeySet(Set<K> keySet)
    {
        if (keySet == null || keySet.isEmpty()) return getAllValues();

        List<V> result = CollectionFactory.newList();

        for (Map.Entry<K, V> entry : keyToValue.entrySet())
        {

            if (keySet.contains(entry.getKey())) continue;

            result.add(entry.getValue());
        }

        return result;
    }

    public final List<V> getAllValues()
    {
        List<V> result = CollectionFactory.newList();

        for (Map.Entry<K, V> entry : keyToValue.entrySet())
        {
            result.add(entry.getValue());
        }

        return result;
    }

    /**
     * For a previously {@link #add(Serializable, Object) added key/value pair}, returns the key corresponding to the
     * given value.
     */
    public final K toKey(V value)
    {
        Defense.notNull(value, "value");

        currentKey = valueToKey.get(value);

        if (currentKey == null) throw new IllegalArgumentException(PublicUtilMessages.missingValue(value, valueToKey
                .keySet()));

        return currentKey;
    }

    public final V toValue(K key)
    {
        V result = keyToValue.get(key);

        if (result == null)
        {
            result = provideMissingObject(key);

            currentKey = key;
        }
        else
        {
            currentKey = key;
        }

        return result;
    }

    /**
     * Invoked by {@link #toValue(Serializable)} whenever a key can not be converted to a value using the internal
     * cache. This is an opportunity to record the fact that an error occured (they key was not valuable, possibly
     * because it points to a deleted entity object) and provide a temporary object. This method may return null, but in
     * a typical application, that will likely case NullPointerExceptions further down the processing chain.
     * <p/>
     * This implementation returns null, and is intended to be overriden in subclasses.
     *
     * @param key key for which a value is required
     * @return a substitute value, or null
     */
    protected V provideMissingObject(K key)
    {
        return null;
    }

    public final boolean isDeleted()
    {
        return inKeySet(deletedKeys);
    }

    public final void setDeleted(boolean value)
    {
        deletedKeys = modifyKeySet(deletedKeys, value);
    }

    /**
     * Returns true if the current key is in the provided set.
     *
     * @param keySet the set of keys to check, or null
     * @return true if the key is in the set, false if it is missing (or if keySet is null)
     */
    protected final boolean inKeySet(Set<K> keySet)
    {
        return keySet != null && keySet.contains(currentKey);
    }

    /**
     * Modifies a keySet to add or remove the current key. If necessary, a new Set is created.
     * <p/>
     * Useage: <code> private Set<K> myFlagKeys;
     * <p/>
     * public boolean void setMyFlag(boolean value) { myFlagKeys = modifySet(myFlagKeys, value); } </code>
     *
     * @param keySet the set of keys, or null
     * @param value  true to add the current key, false to remove
     * @return the provided key set, or a new one
     */
    protected final Set<K> modifyKeySet(Set<K> keySet, boolean value)
    {
        if (keySet == null)
        {
            if (!value) return null;

            keySet = CollectionFactory.newSet();
        }

        if (value) keySet.add(currentKey);
        else keySet.remove(currentKey);

        return keySet;
    }

    /**
     * Does nothing. Subclasses may override as necessary.
     */
    public void prepareForKeys(List<K> keys)
    {
    }
}
