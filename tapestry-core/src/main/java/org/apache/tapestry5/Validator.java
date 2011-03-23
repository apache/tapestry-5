// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.services.FormSupport;

/**
 * Used by a {@link Field} to enforce a <strong>constraint</strong> related to a form submission. Validators themselves
 * are stateless singletons.
 * <p/>
 * Validators are usually encapsulated inside a {@link FieldValidator}.
 *
 * @see FieldValidationSupport
 * @see org.apache.tapestry5.services.FieldValidatorDefaultSource
 */
public interface Validator<C, T>
{
    /**
     * Returns the type of constraint value used with this validator. Constraint values are used to parameterize a
     * validator, for example a "maxLength" validator will have a constraint value of type int (the maximum length
     * allowed). For constraints that do not have a constraint value, this method returns null.
     */
    Class<C> getConstraintType();

    /**
     * Returns the value type associated with this validator. {@link #validate(Field, Object, MessageFormatter, Object)}
     * will only be invoked when the value is assignable to the validator's value type.
     */
    Class<T> getValueType();

    /**
     * Returns the message key, within the validation messages, normally used by this validator. This is used to provide
     * the {@link MessageFormatter} passed to {@link #validate(Field, Object, MessageFormatter, Object)} (unless
     * overridden).
     *
     * @return a message key
     * @see org.apache.tapestry5.services.ValidationMessagesSource
     */
    String getMessageKey();

    /**
     * Invoked after the client-submitted value has been {@link org.apache.tapestry5.Translator translated} to check
     * that the value conforms to expectations (often, in terms of minimum or maximum value). If and only if the value
     * is approved by all Validators is the value applied by the field.
     *
     * @param field           the field for which a client submitted value is being validated
     * @param constraintValue the value used to constrain
     * @param formatter       Validation messages, in the appropriate locale
     * @param value           the translated value supplied by the user
     * @throws ValidationException if the value violates the constraint
     */
    void validate(Field field, C constraintValue, MessageFormatter formatter, T value) throws ValidationException;

    /**
     * Returns true if the validator should be invoked for null or blank (empty string) values. This is generally false,
     * but is true for validators that enforce that a non-blank value is required.  This is the basis of the {@link
     * org.apache.tapestry5.Field#isRequired()} property.
     */
    boolean isRequired();

    /**
     * Hook used by components to allow the validator to contribute additional attributes or (more often) client-side
     * JavaScript (via the {@link FormSupport#addValidation(Field, String, String, Object)}).
     *
     * @param field           the field which is currently being rendered
     * @param constraintValue the value used to constrain input
     * @param formatter       validation message, in the appropriate locale
     * @param writer          markup writer, allowing additional attributes to be written into the active element
     * @param formSupport     used to add JavaScript
     */
    void render(Field field, C constraintValue, MessageFormatter formatter, MarkupWriter writer,
                FormSupport formSupport);
}
