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

package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Field;

import java.text.ParseException;

/**
 * Used to generate the client-side JSON specification for how a number-based validator operates. Uses {@link
 * org.apache.tapestry5.ioc.services.ThreadLocale} to determine the locale for any locale-specific operations.
 *
 * @since 5.1.0.1
 */
public interface NumericTranslatorSupport
{
    /**
     * Parses a client-submitted value in a localized manner.
     *
     * @param type        desired type of value
     * @param clientValue value from client; this will be trimmed of leading/trailing whitespace
     * @param <T>
     * @return the parsed value
     * @throws ParseException
     * @see org.apache.tapestry5.Translator#parseClient(org.apache.tapestry5.Field, String, String)
     */
    <T extends Number> T parseClient(Class<T> type, String clientValue) throws ParseException;

    /**
     * Converts a server-side value to a client-side string. Integer types are formatted simply; decimal types may be
     * formatted using thousands-seperator commas.
     *
     * @param type  type of value to convert
     * @param value current (non-null) value
     * @param <T>
     * @return value formatted
     */
    <T extends Number> String toClient(Class<T> type, T value);

    /**
     * Returns the default message key for parse failures for the indicated type.
     *
     * @param type
     * @param <T>
     * @return a message key: either "integer-format-exception" or "number-format-exception"
     */
    <T extends Number> String getMessageKey(Class<T> type);

    /**
     * Adds client-side format validation for the field, appropriate to the indicated type.
     *
     * @param type    value type
     * @param field   field to which validation should be added
     * @param message message if the client-side value can't be parsed as a number
     * @param <T>
     */
    <T extends Number> void addValidation(Class<T> type, Field field, String message);
}
