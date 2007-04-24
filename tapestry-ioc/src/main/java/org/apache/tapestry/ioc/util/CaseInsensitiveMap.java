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

package org.apache.tapestry.ioc.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An mapped collection where the keys are always strings and access to values is case-insensitive.
 * The case of keys in the map is <em>maintained</em>, but on any access to a key (directly or
 * indirectly), all key comparisons are performed in a case-insensitive manner. The map
 * implementation is intended to support a reasonably finite number (dozens or hundreds, not
 * thousands or millions of key/value pairs. Unlike HashMap, it is based on a sorted list of entries
 * rather than hash bucket. It is also geared towards a largely static map, one that is created and
 * then used without modification.
 * 
 * @param <V>
 *            the type of value stored
 */
public class CaseInsensitiveMap<V> extends AbstractMap<String, V> implements Serializable
{
    private static final long serialVersionUID = 3362718337611953298L;

    private static final int NULL_HASH = Integer.MIN_VALUE;

    private static final int DEFAULT_SIZE = 20;

    private static class CIMEntry<V> implements Map.Entry<String, V>, Serializable
    {
        private static final long serialVersionUID = 6713986085221148350L;

        private String _key;

        private final int _hashCode;

        V _value;

        public CIMEntry(final String key, final int hashCode, V value)
        {
            _key = key;
            _hashCode = hashCode;
            _value = value;
        }

        public String getKey()
        {
            return _key;
        }

        public V getValue()
        {
            return _value;
        }

        public V setValue(V value)
        {
            V result = _value;

            _value = value;

            return result;
        }

        /**
         * Returns true if both keys are null, or if the provided key is the same as, or
         * case-insensitively equal to, the entrie's key.
         * 
         * @param key
         *            to compare against
         * @return true if equal
         */
        boolean matches(String key)
        {
            return key == _key || (key != null && key.equalsIgnoreCase(_key));
        }

        boolean valueMatches(Object value)
        {
            return value == _value || (value != null && value.equals(_value));
        }
    }

    private class EntrySetIterator implements Iterator
    {
        int _expectedModCount = _modCount;

        int _index;

        int _current = -1;

        public boolean hasNext()
        {
            return _index < _size;
        }

        public Object next()
        {
            check();

            if (_index >= _size) throw new NoSuchElementException();

            _current = _index++;

            return _entries[_current];
        }

        public void remove()
        {
            check();

            if (_current < 0) throw new NoSuchElementException();

            new Position(_current, true).remove();

            _expectedModCount = _modCount;
        }

        private void check()
        {
            if (_expectedModCount != _modCount) throw new ConcurrentModificationException();
        }
    }

    @SuppressWarnings("unchecked")
    private class EntrySet extends AbstractSet
    {
        @Override
        public Iterator iterator()
        {
            return new EntrySetIterator();
        }

        @Override
        public int size()
        {
            return _size;
        }

        @Override
        public void clear()
        {
            CaseInsensitiveMap.this.clear();
        }

        @Override
        public boolean contains(Object o)
        {
            if (!(o instanceof Map.Entry)) return false;

            Map.Entry e = (Map.Entry) o;

            Position position = select(e.getKey());

            return position.isFound() ? position.entry().valueMatches(e.getValue()) : false;
        }

        @Override
        public boolean remove(Object o)
        {
            if (!(o instanceof Map.Entry)) return false;

            Map.Entry e = (Map.Entry) o;

            Position position = select(e.getKey());

            if (position.isFound() && position.entry().valueMatches(e.getValue()))
            {
                position.remove();
                return true;
            }

            return false;
        }

    }

    private class Position
    {
        private final int _cursor;

        private final boolean _found;

        Position(int cursor, boolean found)
        {
            _cursor = cursor;
            _found = found;
        }

        boolean isFound()
        {
            return _found;
        }

        CIMEntry<V> entry()
        {
            return _entries[_cursor];
        }

        V get()
        {
            return _found ? _entries[_cursor]._value : null;
        }

        V remove()
        {
            if (!_found) return null;

            V result = _entries[_cursor]._value;

            // Remove the entry by shifting everything else down.

            System.arraycopy(_entries, _cursor + 1, _entries, _cursor, _size - _cursor - 1);

            // We shifted down, leaving one (now duplicate) entry behind.

            _entries[--_size] = null;

            // A structural change for sure

            _modCount++;

            return result;
        }

        @SuppressWarnings("unchecked")
        V put(String key, int hashCode, V newValue)
        {
            if (_found)
            {
                CIMEntry<V> e = _entries[_cursor];

                V result = e._value;

                // Not a structural change, so no change to _modCount

                // Update the key (to maintain case). By definition, the hash code
                // will not change.

                e._key = key;
                e._value = newValue;

                return result;
            }

            // Not found, we're going to add it.

            int newSize = _size + 1;

            if (newSize == _entries.length)
            {
                // Time to expand!

                int newCapacity = (_size * 3) / 2 + 1;

                CIMEntry<V>[] newEntries = new CIMEntry[newCapacity];

                System.arraycopy(_entries, 0, newEntries, 0, _cursor);

                System.arraycopy(_entries, _cursor, newEntries, _cursor + 1, _size - _cursor);

                _entries = newEntries;
            }
            else
            {
                // Open up a space for the new entry

                System.arraycopy(_entries, _cursor, _entries, _cursor + 1, _size - _cursor);
            }

            CIMEntry<V> newEntry = new CIMEntry<V>(key, hashCode, newValue);
            _entries[_cursor] = newEntry;

            _size++;

            // This is definately a structural change

            _modCount++;

            return null;
        }

    }

    // The list of entries. This is kept sorted by hash code. In some cases, there may be different
    // keys with the same hash code in adjacent indexes.
    private CIMEntry<V>[] _entries;

    private int _size = 0;

    // Used by iterators to check for concurrent modifications

    private transient int _modCount = 0;

    private transient Set<Map.Entry<String, V>> _entrySet;

    public CaseInsensitiveMap()
    {
        this(DEFAULT_SIZE);
    }

    @SuppressWarnings("unchecked")
    public CaseInsensitiveMap(int size)
    {
        _entries = new CIMEntry[Math.max(size, 3)];
    }

    public CaseInsensitiveMap(Map<String, ? extends V> map)
    {
        this(map.size());

        for (Map.Entry<String, ? extends V> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < _size; i++)
            _entries[i] = null;

        _size = 0;
        _modCount++;
    }

    @Override
    public boolean isEmpty()
    {
        return _size == 0;
    }

    @Override
    public int size()
    {
        return _size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(String key, V value)
    {
        int hashCode = caseInsenitiveHashCode(key);

        return select(key, hashCode).put(key, hashCode, value);
    }

    @Override
    public boolean containsKey(Object key)
    {
        return select(key).isFound();
    }

    @Override
    public V get(Object key)
    {
        return select(key).get();
    }

    @Override
    public V remove(Object key)
    {
        return select(key).remove();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Map.Entry<String, V>> entrySet()
    {
        if (_entrySet == null) _entrySet = new EntrySet();

        return _entrySet;
    }

    private Position select(Object key)
    {
        if (key == null || key instanceof String)
        {
            String keyString = (String) key;
            return select(keyString, caseInsenitiveHashCode(keyString));
        }

        return new Position(0, false);
    }

    /**
     * Searches the elements for the index of the indicated key and (case insensitive) hash code.
     * Sets the _cursor and _found attributes.
     */
    private Position select(String key, int hashCode)
    {
        if (_size == 0) return new Position(0, false);

        int low = 0;
        int high = _size - 1;

        int cursor = 0;

        while (low <= high)
        {
            cursor = (low + high) >> 1;

            CIMEntry e = _entries[cursor];

            if (e._hashCode < hashCode)
            {
                low = cursor + 1;
                continue;
            }

            if (e._hashCode > hashCode)
            {
                high = cursor - 1;
                continue;
            }

            return tunePosition(key, hashCode, cursor);
        }

        return new Position(low, false);
    }

    /**
     * select() has located a matching hashCode, but there's an outlying possibility that multiple
     * keys share the same hashCode. Backup the cursor until we get to locate the initial hashCode
     * match, then march forward until the key is located, or the hashCode stops matching.
     * 
     * @param key
     * @param hashCode
     */
    private Position tunePosition(String key, int hashCode, int cursor)
    {
        boolean found = false;

        while (cursor > 0)
        {
            if (_entries[cursor - 1]._hashCode != hashCode) break;

            cursor--;
        }

        while (true)
        {
            if (_entries[cursor].matches(key))
            {
                found = true;
                break;
            }

            // Advance to the next entry.

            cursor++;

            // If out of entries,
            if (cursor >= _size || _entries[cursor]._hashCode != hashCode) break;
        }

        return new Position(cursor, found);
    }

    static int caseInsenitiveHashCode(String input)
    {
        if (input == null) return NULL_HASH;

        int length = input.length();
        int hash = 0;

        // This should end up more or less equal to input.toLowerCase().hashCode(), unless String
        // changes its implementation. Let's hope this is reasonably fast.

        for (int i = 0; i < length; i++)
        {
            int ch = input.charAt(i);

            int caselessCh = Character.toLowerCase(ch);

            hash = 31 * hash + caselessCh;
        }

        return hash;
    }

}
