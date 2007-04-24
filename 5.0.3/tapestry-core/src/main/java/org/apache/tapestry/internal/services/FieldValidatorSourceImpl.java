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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.cast;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.Validator;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.FieldValidatorSource;
import org.apache.tapestry.services.ValidationMessagesSource;

public class FieldValidatorSourceImpl implements FieldValidatorSource
{
    private final ValidationMessagesSource _messagesSource;

    private final Map<String, Validator> _validators;

    private final TypeCoercer _typeCoercer;

    private final PageRenderSupport _pageRenderSupport;

    public FieldValidatorSourceImpl(ValidationMessagesSource messagesSource,
            TypeCoercer typeCoercer, PageRenderSupport pageRenderSupport,
            Map<String, Validator> validators)
    {
        _messagesSource = messagesSource;
        _typeCoercer = typeCoercer;
        _pageRenderSupport = pageRenderSupport;
        _validators = validators;
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

        return createValidator(
                field,
                validatorType,
                constraintValue,
                overrideId,
                overrideMessages,
                locale);
    }

    public FieldValidator createValidator(Field field, String validatorType,
            String constraintValue, String overrideId, Messages overrideMessages, Locale locale)
    {
        notBlank(validatorType, "validatorType");

        Validator validator = _validators.get(validatorType);

        if (validator == null)
            throw new IllegalArgumentException(ServicesMessages.unknownValidatorType(
                    validatorType,
                    InternalUtils.sortedKeys(_validators)));

        Object coercedConstraintValue = coerceConstraintValue(constraintValue, validator
                .getConstraintType());

        MessageFormatter formatter = findMessageFormatter(
                overrideId,
                overrideMessages,
                locale,
                validatorType,
                validator);

        return new FieldValidatorImpl(field, coercedConstraintValue, formatter, validator,
                _pageRenderSupport);
    }

    private MessageFormatter findMessageFormatter(String overrideId, Messages overrideMessages,
            Locale locale, String validatorType, Validator validator)
    {

        String overrideKey = overrideId + "-" + validatorType;

        if (overrideMessages.contains(overrideKey))
            return overrideMessages.getFormatter(overrideKey);

        Messages messages = _messagesSource.getValidationMessages(locale);

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

        if (fieldValidators.size() == 1)
            return fieldValidators.get(0);

        return new CompositeFieldValidator(fieldValidators);
    }

    @SuppressWarnings("unchecked")
    private Object coerceConstraintValue(String constraintValue, Class constraintType)
    {
        if (constraintType == null)
            return null;

        return _typeCoercer.coerce(constraintValue, constraintType);
    }

    /**
     * A code defining what the parser is looking for.
     */
    enum State {

        /** The start of a validator type. */
        TYPE_START,
        /** The end of a validator type. */
        TYPE_END,
        /** Equals sign after a validator type, or a comma. */
        EQUALS_OR_COMMA,
        /** The start of a constraint value. */
        VALUE_START,
        /** The end of the constraint value. */
        VALUE_END,
        /** The comma after a constraint value. */
        COMMA
    };

    static List<ValidatorSpecification> parse(String specification)
    {
        List<ValidatorSpecification> result = newList();

        char[] input = specification.toCharArray();

        int cursor = 0;
        int start = -1;

        String type = null;
        String value = null;
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
                        value = specification.substring(start, cursor);

                        result.add(new ValidatorSpecification(type, value));
                        type = null;
                        value = null;

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
                value = specification.substring(start);

                result.add(new ValidatorSpecification(type, value));
                break;

            // For better or worse, ending the string with a comma is valid.

            default:

        }

        return result;
    }

    private static void parseError(int cursor, String specification)
    {
        throw new RuntimeException(ServicesMessages.validatorSpecificationParseError(
                cursor,
                specification));
    }
}
