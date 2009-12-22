// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import java.io.Serializable;
import java.util.List;

/**
 * Used by {@link org.apache.tapestry5.corelib.components.Loop}, {@link org.apache.tapestry5.corelib.components.AjaxFormLoop}
 * and similar components to extract out an identifier, here termed a "primary key", that can be stored on the client
 * and later used to recover the same, or equivalent, server side object.
 * <p/>
 * The {@link org.apache.tapestry5.util.DefaultPrimaryKeyEncoder} implementation is used in most circumstances.
 *
 * @param <K> the type of the primary key, used to identify the value (which must be serializable)
 * @param <V> the type of value identified by the key
 * @see org.apache.tapestry5.ValueEncoder
 */
public interface PrimaryKeyEncoder<K extends Serializable, V>
{
    /**
     * Given a particular value, this method extracts and returns the primary key that identifies the value. The key
     * will later be converted back into a value using {@link #toValue(Serializable)}.
     *
     * @param value whose primary key is needed
     * @return the key for the value
     */
    K toKey(V value);

    /**
     * Invoked as part of a form submission to alert the encoder that a series of keys may be converted back to values.
     * This is advisory only, and the keys passed to {@link #toValue(Serializable)} may not include all keys in the
     * list, or may include keys not in the list. In general, though, the keys passed in will match the actual keys to
     * be converted, giving the encoder a chance to efficiently fetch the necessary value objects as a group.
     */
    void prepareForKeys(List<K> keys);

    /**
     * For a particular primary key, previously obtained via {@link #toKey(Object)}, this method returns the same or
     * equivalent object.
     *
     * @param key used to identify the object
     * @return the value object for the key
     */
    V toValue(K key);
}
