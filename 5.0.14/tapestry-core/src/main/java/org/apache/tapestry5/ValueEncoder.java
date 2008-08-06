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

/**
 * Used to convert server side values to client-side strings.  This is used when generating a {@link
 * org.apache.tapestry5.EventContext} as part of a URL, or when components (such as {@link
 * org.apache.tapestry5.corelib.components.Select}) generated other client-side strings.
 * <p/>
 * Often a custom implementation is needed for entity type objects, where the {@link #toClient(Object)} method extracts
 * a primary key, and the {@link #toValue(String)} re-acquires the corresponding entity object.
 *
 * @see SelectModel
 * @see org.apache.tapestry5.services.ValueEncoderSource
 * @see PrimaryKeyEncoder
 */
public interface ValueEncoder<V>
{
    /**
     * Converts a value into a client-side representation. The value should be parseable by {@link #toValue(String)}. In
     * some cases, what is returned is an identifier used to locate the true object, rather than a string representation
     * of the value itself.
     *
     * @param value to be encoded
     * @return a string representation of the value, or the value's identity
     */
    String toClient(V value);

    /**
     * Converts a client-side representation, provided by {@link #toClient(Object)}, back into a server-side value.
     *
     * @param clientValue string representation of the value's identity
     * @return the corresponding entity, or null if not found
     */
    V toValue(String clientValue);
}
