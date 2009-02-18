// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Creates "interned" strings that are unique for the same content. This is used for common description strings,
 * particularly those used by {@link org.apache.tapestry5.Binding} instances.  The internal cache of interned strings id
 * cleared whenever the {@link org.apache.tapestry5.services.ComponentClasses} {@link
 * org.apache.tapestry5.services.InvalidationEventHub} is invalidated (i.e., when component class files change).
 *
 * @since 5.1.0.0
 */
public interface StringInterner
{
    /**
     * Interns a string.
     *
     * @param string the string to intern
     * @return the input string, or another string instance with the same content
     */
    String intern(String string);

    /**
     * Formats a string (using {@link String#format(String, Object[])}) and returns the interned result.
     *
     * @param format    string format
     * @param arguments used inside the format
     * @return formatted and interned string
     */
    String format(String format, Object... arguments);
}
