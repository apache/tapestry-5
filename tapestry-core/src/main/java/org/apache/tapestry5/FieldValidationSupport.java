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
 * Services to help with field {@linkplain org.apache.tapestry5.Validator validation} and {@linkplain
 * org.apache.tapestry5.Translator translation}. This service encapsulates the logic that mixes normal
 * configured/declared validation/translation with events triggered on the component.
 */
public interface FieldValidationSupport
{
    /**
     * A wrapper around {@link org.apache.tapestry5.Translator#toClient(Object)} that first fires a "toclient" event on
     * the component to see if it can perform the conversion. If the value is null, then no event is fired and the
     * translator is <em>not</em> invoked, the return value is simply null.
     *
     * @param value              to be converted to a client-side string, which may be null
     * @param componentResources used to fire events on the component
     * @param translator         used if the component does not provide a non-null value
     * @param nullFieldStrategy  used to convert a null server side value to an appropriate client side value
     * @return the translated value  or null if the value is null
     * @see org.apache.tapestry5.Translator#toClient(Object)
     */
    String toClient(Object value, ComponentResources componentResources, FieldTranslator<Object> translator,
                    NullFieldStrategy nullFieldStrategy);

    /**
     * A wrapper around {@link Translator#parseClient(Field, String, String)}. First a "parseclient" event is fired; the
     * translator is only invoked if that returns null (typically because there is no event handler method for the
     * event).
     *
     * @param clientValue        the value provided by the client (not null)
     * @param componentResources used to trigger events
     * @param translator         translator that will do the work if the component event returns null
     * @param nullFieldStrategy  used to convert null/blank values from client into non-null server side values
     * @return the input parsed to an object
     * @throws org.apache.tapestry5.ValidationException
     *          if the value can't be parsed
     * @see Translator#parseClient(Field, String, String)
     */
    Object parseClient(String clientValue, ComponentResources componentResources, FieldTranslator<Object> translator,
                       NullFieldStrategy nullFieldStrategy)
            throws ValidationException;

    /**
     * Performs validation on a parsed value from the client.  Normal validations occur first, then a "validate" event
     * is triggered on the component.
     *
     * @param value              parsed value from the client, possibly null
     * @param componentResources used to trigger events
     * @param validator          performs normal validations
     * @throws ValidationException if the value is not valid
     * @see org.apache.tapestry5.Validator#validate(Field, Object, org.apache.tapestry5.ioc.MessageFormatter, Object)
     */
    void validate(Object value, ComponentResources componentResources, FieldValidator validator)
            throws ValidationException;
}
