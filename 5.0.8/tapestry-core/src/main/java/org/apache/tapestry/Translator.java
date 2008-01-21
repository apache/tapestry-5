// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.ValidationMessagesSource;

/**
 * Translates between client-side and server-side values. Client-side values are always strings.
 *
 * @param <T>
 * @see org.apache.tapestry.services.TranslatorDefaultSource
 * @see FieldValidationSupport
 */
public interface Translator<T>
{
    /**
     * Converts a server-side value to a client-side string. This allows for formatting of the value in a way
     * appropriate to the end user. The output client value should be parsable by {@link #parseClient(String,
     * Messages)}.
     *
     * @param value the server side value (which will not be null)
     * @return client-side value to present to the user
     */
    String toClient(T value);

    /**
     * Converts a submitted request value into an appropriate server side value.
     *
     * @param clientValue to convert to a server value; this will not be the empty string or null
     * @param messages    validator messages assembled by {@link ValidationMessagesSource}
     * @return equivalent server-side value (possibly null)
     * @throws ValidationException if the value can not be parsed
     */
    T parseClient(String clientValue, Messages messages) throws ValidationException;
}
