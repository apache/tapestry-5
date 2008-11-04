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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry5.ioc.internal.util.Defense.cast;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.ValidationMessagesSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FieldValidatorSourceImpl implements FieldValidatorSource
{
    private final ValidationMessagesSource messagesSource;

    private final Map<String, Validator> validators;

    private final TypeCoercer typeCoercer;

    private final FormSupport formSupport;

    public FieldValidatorSourceImpl(ValidationMessagesSource messagesSource, TypeCoercer typeCoercer,
                                    FormSupport formSupport, Map<String, Validator> validators)
    {
        this.messagesSource = messagesSource;
        this.typeCoercer = typeCoercer;
        this.formSupport = formSupport;
        this.validators = validators;
    }

    public FieldValidator createValidator(Field field, String validatorType, String constraintValue)
    {
        Component component = cast(field, Component.class, "field");
        notBlank(validatorType, "validatorType");

        ComponentResources componentResources = component.getComponentResources();
        String overrideId = componentResources.getId();
        Locale locale = componentResources.getLocale();

        // So, if you use a TextField on your EditUser page, we want to search the messages
        // of the EditUser page (the container), not the TextField (which will always be the same).

        Messages overrideMessages = componentResources.getContainerMessages();

        return createValidator(field, validatorType, constraintValue, overrideId, overrideMessages, locale);
    }

    public FieldValidator createValidator(Field field, String validatorType, String constraintValue, String overrideId,
                                          Messages overrideMessages, Locale locale)
    {
        notBlank(validatorType, "validatorType");

        Validator validator = validators.get(validatorType);

        if (validator == null)
            throw new IllegalArgumentException(
                    ServicesMessages.unknownValidatorType(validatorType, InternalUtils.sortedKeys(validators)));

        // I just have this thing about always treating parameters as finals, so
        // we introduce a second variable to treat a mutable.

        String formValidationid = formSupport.getFormValidationId();

        Object coercedConstraintValue = computeConstraintValue(validatorType, validator, constraintValue,
                                                               formValidationid,
                                                               overrideId,
                                                               overrideMessages);

        MessageFormatter formatter = findMessageFormatter(formValidationid, overrideId, overrideMessages, locale,
                                                          validatorType,
                                                          validator);

        return new FieldValidatorImpl(field, coercedConstraintValue, formatter, validator, formSupport);
    }

    private Object computeConstraintValue(String validatorType, Validator validator, String constraintValue,
                                          String formId, String overrideId,
                                          Messages overrideMessages)
    {
        Class constraintType = validator.getConstraintType();

        String constraintText = findConstraintValue(validatorType, constraintType, constraintValue, formId, overrideId,
                                                    overrideMessages);

        if (constraintText == null) return null;

        return typeCoercer.coerce(constraintText, constraintType);
    }

    private String findConstraintValue(String validatorType, Class constraintType, String constraintValue,
                                       String formValidationId, String overrideId,
                                       Messages overrideMessages)
    {
        if (constraintValue != null) return constraintValue;

        if (constraintType == null) return null;

        // If no constraint was provided, check to see if it is available via a localized message
        // key. This is really handy for complex validations such as patterns.

        String perFormKey = formValidationId + "-" + overrideId + "-" + validatorType;

        if (overrideMessages.contains(perFormKey)) return overrideMessages.get(perFormKey);

        String generalKey = overrideId + "-" + validatorType;

        if (overrideMessages.contains(generalKey)) return overrideMessages.get(generalKey);

        throw new IllegalArgumentException(
                ServicesMessages.missingValidatorConstraint(validatorType, constraintType, perFormKey, generalKey));
    }

    private MessageFormatter findMessageFormatter(String formId, String overrideId, Messages overrideMessages,
                                                  Locale locale,
                                                  String validatorType, Validator validator)
    {

        String overrideKey = formId + "-" + overrideId + "-" + validatorType + "-message";

        if (overrideMessages.contains(overrideKey)) return overrideMessages.getFormatter(overrideKey);

        overrideKey = overrideId + "-" + validatorType + "-message";

        if (overrideMessages.contains(overrideKey)) return overrideMessages.getFormatter(overrideKey);

        Messages messages = messagesSource.getValidationMessages(locale);

        String key = validator.getMessageKey();

        return messages.getFormatter(key);
    }

    public FieldValidator createValidators(Field field, String specification)
    {
        List<ValidatorSpecification> specs = parse(specification);

        List<FieldValidator> fieldValidators = newList();

        for (ValidatorSpecification spec : specs)
        {
            fieldValidators.add(createValidator(field, spec.getValidatorType(), spec
                    .getConstraintValue()));
        }

        if (fieldValidators.size() == 1) return fieldValidators.get(0);

        return new CompositeFieldValidator(fieldValidators);
    }

    /**
     * A code defining what the parser is looking for.
     */
    enum State
    {

        /**
         * The start of a validator type.
         */
        TYPE_START,
        /**
         * The end of a validator type.
         */
        TYPE_END,
        /**
         * Equals sign after a validator type, or a comma.
         */
        EQUALS_OR_COMMA,
        /**
         * The start of a constraint value.
         */
        VALUE_START,
        /**
         * The end of the constraint value.
         */
        VALUE_END,
        /**
         * The comma after a constraint value.
         */
        COMMA
    }

    static List<ValidatorSpecification> parse(String specification)
    {
        List<ValidatorSpecification> result = newList();

        char[] input = specification.toCharArray();

        int cursor = 0;
        int start = -1;

        String type = null;
        boolean skipWhitespace = true;
        State state = State.TYPE_START;

        while (cursor < input.length)
        {
            char ch = input[cursor];

            if (skipWhitespace && Character.isWhitespace(ch))
            {
                cursor++;
                continue;
            }

            skipWhitespace = false;

            switch (state)
            {

                case TYPE_START:

                    if (Character.isLetter(ch))
                    {
                        start = cursor;
                        state = State.TYPE_END;
                        break;
                    }

                    parseError(cursor, specification);

                case TYPE_END:

                    if (Character.isLetter(ch))
                    {
                        break;
                    }

                    type = specification.substring(start, cursor);

                    skipWhitespace = true;
                    state = State.EQUALS_OR_COMMA;
                    continue;

                case EQUALS_OR_COMMA:

                    if (ch == '=')
                    {
                        skipWhitespace = true;
                        state = State.VALUE_START;
                        break;
                    }

                    if (ch == ',')
                    {
                        result.add(new ValidatorSpecification(type));
                        type = null;
                        state = State.COMMA;
                        continue;
                    }

                    parseError(cursor, specification);

                case VALUE_START:

                    start = cursor;
                    state = State.VALUE_END;
                    break;

                case VALUE_END:

                    // The value ends when we hit whitespace or a comma

                    if (Character.isWhitespace(ch) || ch == ',')
                    {
                        String value = specification.substring(start, cursor);

                        result.add(new ValidatorSpecification(type, value));
                        type = null;

                        skipWhitespace = true;
                        state = State.COMMA;
                        continue;
                    }

                    break;

                case COMMA:

                    if (ch == ',')
                    {
                        skipWhitespace = true;
                        state = State.TYPE_START;
                        break;
                    }

                    parseError(cursor, specification);
            } // case

            cursor++;
        } // while

        // cursor is now one character past end of string.
        // Cleanup whatever state we were in the middle of.

        switch (state)
        {
            case TYPE_END:

                type = specification.substring(start);

            case EQUALS_OR_COMMA:

                result.add(new ValidatorSpecification(type));
                break;

                // Case when the specification ends with an equals sign.

            case VALUE_START:
                result.add(new ValidatorSpecification(type, ""));
                break;

            case VALUE_END:

                result.add(new ValidatorSpecification(type, specification.substring(start)));
                break;

                // For better or worse, ending the string with a comma is valid.

            default:
        }

        return result;
    }

    private static void parseError(int cursor, String specification)
    {
        throw new RuntimeException(ServicesMessages.validatorSpecificationParseError(cursor, specification));
    }
}
