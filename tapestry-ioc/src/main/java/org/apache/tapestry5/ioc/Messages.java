// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

/**
 * Provides access to a messages catalog, a set of properties files that provide localized messages for a particular
 * locale. The message catalog consists of keys and values and follows the semantics of a Java {@link
 * java.util.ResourceBundle} with some changes.
 */
public interface Messages
{
    /**
     * Returns true if the bundle contains the named key.
     */
    boolean contains(String key);

    /**
     * Returns the localized message for the given key. If catalog does not contain such a key, then a modified version
     * of the key is returned (converted to upper case and enclosed in brackets).
     *
     * @param key
     * @return localized message for key, or placeholder
     */
    String get(String key);

    /**
     * Returns a formatter for the message, which can be used to substitute arguments (as per {@link
     * java.util.Formatter}).
     *
     * @param key
     * @return formattable object
     */
    MessageFormatter getFormatter(String key);

    /**
     * Convienience for accessing a formatter and formatting a localized message with arguments.
     */

    String format(String key, Object... args);
}
