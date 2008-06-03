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

package org.apache.tapestry5.ioc.util;

import java.io.Serializable;
import java.util.*;

/**
 * An mapped collection where the keys are always strings and access to values is case-insensitive. The case of keys in
 * the map is <em>maintained</em>, but on any access to a key (directly or indirectly), all key comparisons are
 * performed in a case-insensitive manner. The map implementation is intended to support a reasonably finite number
 * (dozens or hundreds, not thousands or millions of key/value pairs. Unlike HashMap, it is based on a sorted list of
 * entries rather than hash bucket. It is also geared towards a largely static map, one that is created and then used
 * without modification.
 *
 * @param <V> the type of value stored
 */
public class CaseInsensitiveMap<V> extends AbstractMap<String, V> implements Serializable
{
    private static final long serialVersionUID = 3362718337611953298L;

    private static final int NULL_HASH = Integer.MIN_VALUE;

    private static final int DEFAULT_SIZE = 20;

    private static class CIMEntry<V> implements Map.Entry<String, V>, Serializable
    {
        private static final long serialVersionUID = 6713986085221148350L;

        private String key;

        private final int hashCode;

        V value;

        public CIMEntry(final String key, final int hashCode, V value)
        {
            this.key = key;
            this.hashCode = hashCode;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }

        public V setValue(V value)
        {
            V result = this.value;

            this.value = value;

            return result;
        }

        /**
         * Returns true if both keys are null, or if the provided key is the same as, or case-insensitively equal to,
         * the entrie's key.
         *
         * @param key to compare against
         * @return true if equal
         */
        @SuppressWarnings({ "StringEquality" })
        boolean matches(String key)
        {
            return key == this.key || (key != null && key.equalsIgnoreCase(this.key));
        }

        boolean valueMatches(Object value)
        {
            return value == this.value || (value != null && value.equals(this.value));
        }
    }

    private class EntrySetIterator implements Iterator
    {
        int expectedModCount = modCount;

        int index;

        int current = -1;

        public boolean hasNext()
        {
            return index < size;
        }

        public Object next()
        {
            check();

            if (index >= size) throw new NoSuchElementException();

            current = index++;

            return entries[current];
        }

        public void remove()
        {
            check();

            if (current < 0) throw new NoSuchElementException();

            new Position(current, true).remove();

            expectedModCount = modCount;
        }

        private void check()
        {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
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
            return size;
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

            return position.isFound() && position.entry().valueMatches(e.getValue());
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
        private final int cursor;

        private final boolean found;

        Position(int cursor, boolean found)
        {
            this.cursor = cursor;
            this.found = found;
        }

        boolean isFound()
        {
            return found;
        }

        CIMEntry<V> entry()
        {
            return entries[cursor];
        }

        V get()
        {
            return found ? entries[cursor].value : null;
        }

        V remove()
        {
            if (!found) return null;

            V result = entries[cursor].value;

            // Remove the entry by shifting everything else down.

            System.arraycopy(entries, cursor + 1, entries, cursor, size - cursor - 1);

            // We shifted down, leaving one (now duplicate) entry behind.

            entries[--size] = null;

            // A structural change for sure

            modCount++;

            return result;
        }

        @SuppressWarnings("unchecked")
        V put(String key, int hashCode, V newValue)
        {
            if (found)
            {
                CIMEntry<V> e = entries[cursor];

                V result = e.value;

                // Not a structural change, so no change to modCount

                // Update the key (to maintain case). By definition, the hash code
                // will not change.

                e.key = key;
                e.value = newValue;

                return result;
            }

            // Not found, we're going to add it.

            int newSize = size + 1;

            if (newSize == entries.length)
            {
                // Time to expand!

                int newCapacity = (size * 3) / 2 + 1;

                CIMEntry<V>[] newEntries = new CIMEntry[newCapacity];

                System.arraycopy(entries, 0, newEntries, 0, cursor);

                System.arraycopy(entries, cursor, newEntries, cursor + 1, size - cursor);

                entries = newEntries;
            }
            else
            {
                // Open up a space for the new entry

                System.arraycopy(entries, cursor, entries, cursor + 1, size - cursor);
            }

            CIMEntry<V> newEntry = new CIMEntry<V>(key, hashCode, newValue);
            entries[cursor] = newEntry;

            size++;

            // This is definately a structural change

            modCount++;

            return null;
        }

    }

    // The list of entries. This is kept sorted by hash code. In some cases, there may be different
    // keys with the same hash code in adjacent indexes.
    private CIMEntry<V>[] entries;

    private int size = 0;

    // Used by iterators to check for concurrent modifications

    private transient int modCount = 0;

    private transient Set<Map.Entry<String, V>> entrySet;

    public CaseInsensitiveMap()
    {
        this(DEFAULT_SIZE);
    }

    @SuppressWarnings("unchecked")
    public CaseInsensitiveMap(int size)
    {
        entries = new CIMEntry[Math.max(size, 3)];
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
        for (int i = 0; i < size; i++)
            entries[i] = null;

        size = 0;
        modCount++;
    }

    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public int size()
    {
        return size;
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
        if (entrySet == null) entrySet = new EntrySet();

        return entrySet;
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
     * Searches the elements for the index of the indicated key and (case insensitive) hash code. Sets the _cursor and
     * _found attributes.
     */
    private Position select(String key, int hashCode)
    {
        if (size == 0) return new Position(0, false);

        int low = 0;
        int high = size - 1;

        int cursor;

        while (low <= high)
        {
            cursor = (low + high) >> 1;

            CIMEntry e = entries[cursor];

            if (e.hashCode < hashCode)
            {
                low = cursor + 1;
                continue;
            }

            if (e.hashCode > hashCode)
            {
                high = cursor - 1;
                continue;
            }

            return tunePosition(key, hashCode, cursor);
        }

        return new Position(low, false);
    }

    /**
     * select() has located a matching hashCode, but there's an outlying possibility that multiple keys share the same
     * hashCode. Backup the cursor until we get to locate the initial hashCode match, then march forward until the key
     * is located, or the hashCode stops matching.
     *
     * @param key
     * @param hashCode
     */
    private Position tunePosition(String key, int hashCode, int cursor)
    {
        boolean found = false;

        while (cursor > 0)
        {
            if (entries[cursor - 1].hashCode != hashCode) break;

            cursor--;
        }

        while (true)
        {
            if (entries[cursor].matches(key))
            {
                found = true;
                break;
            }

            // Advance to the next entry.

            cursor++;

            // If out of entries,
            if (cursor >= size || entries[cursor].hashCode != hashCode) break;
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
