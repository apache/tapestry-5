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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.util.Locale;

/**
 * Used to create {@link org.apache.tapestry5.FieldValidator}s for a particular {@link org.apache.tapestry5.Field}
 * component.
 */
@UsesMappedConfiguration(Validator.class)
public interface FieldValidatorSource
{
    /**
     * Creates the validator. The error message associated with the field validator usually comes from the {@link
     * ValidationMessagesSource} (using the validator's {@link Validator#getMessageKey() message key}). However, if the
     * container component of the field defines a message key <code><i>id</i>-<i>validator</i> (where id is the simple
     * id of the field component, and validator is the validatorType), then that message is used instead. This allows
     * you to override the message for a particular validation of a particular field.
     *
     * @param field           the field for which a validator is to be created
     * @param validatorType   used to select the {@link org.apache.tapestry5.Validator} that forms the core of the
     *                        {@link org.apache.tapestry5.FieldValidator}
     * @param constraintValue a value used to configure the validator, or null if the validator is not configurarable
     * @return the field validator for the field
     */
    FieldValidator createValidator(Field field, String validatorType, String constraintValue);

    /**
     * Full featured version of {@link #createValidator(Field, String, String)} used in situations where the container
     * of the field is not necesarrilly the place to look for override messages, and the id of the field is not the key
     * to use when checking. The {@link BeanEditForm} is an example of this.
     *
     * @param field            the field for which a validator is to be created
     * @param validatorType    used to select the {@link org.apache.tapestry5.Validator} that forms the core of the
     *                         {@link org.apache.tapestry5.FieldValidator}
     * @param constraintValue  a value used to configure the validator, or null if the validator is not configurable
     * @param overrideId       the base id used when searching for validator message overrides (this would normally be
     *                         the field component's simple id)
     * @param overrideMessages the message catalog to search for override messages (this would normally be the catalog
     *                         for the container of the field component)
     * @param locale           locale used when retrieving default validation messages from the {@link
     *                         org.apache.tapestry5.services.ValidationMessagesSource}
     * @return the field validator for the field
     */
    FieldValidator createValidator(Field field, String validatorType, String constraintValue,
                                   String overrideId, Messages overrideMessages, Locale locale);

    /**
     * Creates a set of validators. The specification is a string used to identify and configure the individual
     * validators. The specification is a comma-separated list of terms. Each term is a validator type name and an
     * optional constraint value (seperated by an equals sign).
     *
     * @param field
     * @param specification
     * @return a composite field validator
     */
    FieldValidator createValidators(Field field, String specification);
}
