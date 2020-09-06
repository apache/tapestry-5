// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.validator.ValidatorMacro;

import static org.apache.tapestry5.commons.util.CollectionFactory.newList;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("all")
public class FieldValidatorSourceImpl implements FieldValidatorSource
{
    private final Messages globalMessages;

    private final Map<String, Validator> validators;

    private final TypeCoercer typeCoercer;

    private final FormSupport formSupport;

    private final ValidatorMacro validatorMacro;

    public FieldValidatorSourceImpl(Messages globalMessages, TypeCoercer typeCoercer,
                                    FormSupport formSupport, Map<String, Validator> validators, ValidatorMacro validatorMacro)
    {
        this.globalMessages = globalMessages;
        this.typeCoercer = typeCoercer;
        this.formSupport = formSupport;
        this.validators = validators;
        this.validatorMacro = validatorMacro;
    }

    public FieldValidator createValidator(Field field, String validatorType, String constraintValue)
    {
        Component component = (Component) field;
        assert InternalUtils.isNonBlank(validatorType);
        ComponentResources componentResources = component.getComponentResources();
        String overrideId = componentResources.getId();

        // So, if you use a TextField on your EditUser page, we want to search the messages
        // of the EditUser page (the container), not the TextField (which will always be the same).

        Messages overrideMessages = componentResources.getContainerMessages();

        return createValidator(field, validatorType, constraintValue, overrideId, overrideMessages, null);
    }

    public FieldValidator createValidator(Field field, String validatorType, String constraintValue, String overrideId,
                                          Messages overrideMessages, Locale locale)
    {

        ValidatorSpecification originalSpec = new ValidatorSpecification(validatorType, constraintValue);

        List<ValidatorSpecification> org = CollectionFactory.newList(originalSpec);

        List<ValidatorSpecification> specs = expandMacros(org);

        List<FieldValidator> fieldValidators = CollectionFactory.<FieldValidator>newList();

        for (ValidatorSpecification spec : specs)
        {
            fieldValidators.add(createValidator(field, spec, overrideId, overrideMessages));
        }

        return new CompositeFieldValidator(fieldValidators);
    }

    private FieldValidator createValidator(Field field, ValidatorSpecification spec, String overrideId,
                                           Messages overrideMessages)
    {

        String validatorType = spec.getValidatorType();

        assert InternalUtils.isNonBlank(validatorType);
        Validator validator = validators.get(validatorType);

        if (validator == null)
            throw new IllegalArgumentException(String.format("Unknown validator type '%s'. Configured validators are %s.", validatorType, InternalUtils.join(InternalUtils.sortedKeys(validators))));

        // I just have this thing about always treating parameters as finals, so
        // we introduce a second variable to treat a mutable.

        String formValidationid = formSupport.getFormValidationId();

        Object coercedConstraintValue = computeConstraintValue(validatorType, validator, spec.getConstraintValue(),
                formValidationid, overrideId, overrideMessages);

        MessageFormatter formatter = findMessageFormatter(formValidationid, overrideId, overrideMessages, validatorType,
                validator);

        return new FieldValidatorImpl(field, coercedConstraintValue, formatter, validator, formSupport);
    }

    private Object computeConstraintValue(String validatorType, Validator validator, String constraintValue,
                                          String formId, String overrideId, Messages overrideMessages)
    {
        Class constraintType = validator.getConstraintType();

        String constraintText = findConstraintValue(validatorType, constraintType, constraintValue, formId, overrideId,
                overrideMessages);

        if (constraintText == null)
            return null;

        return typeCoercer.coerce(constraintText, constraintType);
    }

    private String findConstraintValue(String validatorType, Class constraintType, String constraintValue,
                                       String formValidationId, String overrideId, Messages overrideMessages)
    {
        if (constraintValue != null)
            return constraintValue;

        if (constraintType == null)
            return null;

        // If no constraint was provided, check to see if it is available via a localized message
        // key. This is really handy for complex validations such as patterns.

        String perFormKey = formValidationId + "-" + overrideId + "-" + validatorType;

        if (overrideMessages.contains(perFormKey))
            return overrideMessages.get(perFormKey);

        String generalKey = overrideId + "-" + validatorType;

        if (overrideMessages.contains(generalKey))
            return overrideMessages.get(generalKey);

        throw new IllegalArgumentException(String.format("Validator '%s' requires a validation constraint (of type %s) but none was provided. The constraint may be provided inside the @Validator annotation on the property, or in the associated component message catalog as key '%s' or key '%s'.", validatorType, constraintType.getName(), perFormKey,
                generalKey));
    }

    private MessageFormatter findMessageFormatter(String formId, String overrideId, Messages overrideMessages,
                                                  String validatorType, Validator validator)
    {

        String overrideKey = formId + "-" + overrideId + "-" + validatorType + "-message";

        if (overrideMessages.contains(overrideKey))
            return overrideMessages.getFormatter(overrideKey);

        overrideKey = overrideId + "-" + validatorType + "-message";

        if (overrideMessages.contains(overrideKey))
            return overrideMessages.getFormatter(overrideKey);

        String key = validator.getMessageKey();

        return globalMessages.getFormatter(key);
    }

    public FieldValidator createValidators(Field field, String specification)
    {
        List<ValidatorSpecification> specs = toValidatorSpecifications(specification);

        List<FieldValidator> fieldValidators = CollectionFactory.newList();

        for (ValidatorSpecification spec : specs)
        {
            fieldValidators.add(createValidator(field, spec.getValidatorType(), spec.getConstraintValue()));
        }

        if (fieldValidators.size() == 1)
            return fieldValidators.get(0);

        return new CompositeFieldValidator(fieldValidators);
    }

    List<ValidatorSpecification> toValidatorSpecifications(String specification)
    {
        return expandMacros(parse(specification));
    }

    private List<ValidatorSpecification> expandMacros(List<ValidatorSpecification> specs)
    {
        Map<String, Boolean> expandedMacros = CollectionFactory.newCaseInsensitiveMap();
        List<ValidatorSpecification> queue = CollectionFactory.newList(specs);
        List<ValidatorSpecification> result = CollectionFactory.newList();

        while (!queue.isEmpty())
        {
            ValidatorSpecification head = queue.remove(0);

            String validatorType = head.getValidatorType();

            String expanded = validatorMacro.valueForMacro(validatorType);
            if (expanded != null)
            {
                if (head.getConstraintValue() != null)
                    throw new RuntimeException(String.format(
                            "'%s' is a validator macro, not a validator, and can not have a constraint value.",
                            validatorType));

                if (expandedMacros.containsKey(validatorType))
                    throw new RuntimeException(String.format("Validator macro '%s' appears more than once.",
                            validatorType));

                expandedMacros.put(validatorType, true);

                List<ValidatorSpecification> parsed = parse(expanded);

                // Add the new validator specifications to the front of the queue, replacing the validator macro

                for (int i = 0; i < parsed.size(); i++)
                {
                    queue.add(i, parsed.get(i));
                }
            } else
            {
                result.add(head);
            }
        }

        return result;
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
        throw new RuntimeException(String.format("Unexpected character '%s' at position %d of input string: %s", specification.charAt(cursor), cursor + 1,
                specification));
    }
}
