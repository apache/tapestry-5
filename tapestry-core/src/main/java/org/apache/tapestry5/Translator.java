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

package org.apache.tapestry5;

import org.apache.tapestry5.services.FormSupport;

/**
 * Translates between client-side and server-side values. Client-side values are always strings.
 *
 * @param <T>
 * @see org.apache.tapestry5.services.TranslatorSource
 * @see org.apache.tapestry5.FieldValidationSupport
 * @see org.apache.tapestry5.FieldTranslator
 */
public interface Translator<T>
{
    /**
     * Returns a unique name for the translator. This is used to identify the translator by name, but is also used when
     * locating override messages for the translator.
     *
     * @return unique name for the translator
     */
    String getName();

    /**
     * Converts a server-side value to a client-side string. This allows for formatting of the value in a way
     * appropriate to the end user. The output client value should be parsable by {@link #parseClient(Field, String,
     * String)}.
     *
     * @param value the server side value (which will not be null)
     * @return client-side value to present to the user
     */
    String toClient(T value);

    /**
     * Returns the type of  the server-side value.
     *
     * @return a type
     */
    Class<T> getType();

    /**
     * Returns the message key, within the validation messages, normally used by this validator. This is used to provide
     * the formatted message to {@link #parseClient(Field, String, String)} or {@link #render(Field, String,
     * MarkupWriter, org.apache.tapestry5.services.FormSupport)}.
     *
     * @return a message key
     * @see org.apache.tapestry5.services.ValidationMessagesSource
     */
    String getMessageKey();

    /**
     * Converts a submitted request value into an appropriate server side value.
     *
     * @param field       for which a value is being parsed
     * @param clientValue to convert to a server value; this will not be null, but may be blank
     * @param message     formatted validation message, either from validation messages, or from an override
     * @return equivalent server-side value (possibly null)
     * @throws ValidationException if the value can not be parsed
     */
    T parseClient(Field field, String clientValue, String message) throws ValidationException;

    /**
     * Hook used by components to allow the validator to contribute additional attributes or (more often) client-side
     * JavaScript (via the {@link org.apache.tapestry5.services.FormSupport#addValidation(Field, String, String,
     * Object)}).
     *
     * @param field       the field which is currently being rendered
     * @param message     formatted validation message, either from validation messages, or from an override
     * @param writer      markup writer, allowing additional attributes to be written into the active element
     * @param formSupport used to add JavaScript
     */
    void render(Field field, String message, MarkupWriter writer,
                FormSupport formSupport);
}
