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

package org.apache.tapestry.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;

/**
 * Services to help with field {@linkplain org.apache.tapestry.Validator validation} and {@linkplain
 * org.apache.tapestry.Translator translation}. This service encapsulates the logic that mixes normal
 * configured/declared validation/translation with events triggered on the component.
 */
public interface FieldValidationSupport
{
    /**
     * A wrapper around {@link org.apache.tapestry.Translator#toClient(Object)} that first fires a "toclient" event on
     * the component to see if it can perform the conversion. If the value is null, then no event is fired and the
     * translator is <em>not</em> invoked, the return value is simply null.
     *
     * @param value              to be converted to a client-side string
     * @param componentResources used to fire events on the component
     * @param translator         used if the component does not provide a non-null value
     * @return the translated value  or null if the value is null
     */
    String toClient(Object value, ComponentResources componentResources, Translator translator);

    /**
     * A wrapper around {@link org.apache.tapestry.Translator#parseClient(String, org.apache.tapestry.ioc.Messages)}.
     * First a "parseclient" event is fired; the translator is only invoked if that returns null.
     * <p/>
     * If the client value is null or blank, then no event is fired and the translator is not invoked.  Instead, the
     * return value is null.
     *
     * @param clientValue        the value provided by the client (not null or blank)
     * @param componentResources used to trigger events
     * @param translator         translator that will do the work if the component event returns null
     * @return the input parsed to an object
     * @throws org.apache.tapestry.ValidationException
     *          if the value can't be parsed
     */
    Object parseClient(String clientValue, ComponentResources componentResources, Translator translator)
            throws ValidationException;

    /**
     * Performs validation on a parsed value from the client.  Normal validations occur first, then a "validate" event
     * is triggered on the component.
     *
     * @param value              parsed value from the client, possibly null
     * @param componentResources used to trigger events
     * @param validator          performs normal validations
     * @throws ValidationException if the value is not valid
     */
    void validate(Object value, ComponentResources componentResources, FieldValidator validator)
            throws ValidationException;
}