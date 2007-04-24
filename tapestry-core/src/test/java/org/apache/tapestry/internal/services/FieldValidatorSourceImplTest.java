// Copyright 2006 The Apache Software Foundation
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

import static java.util.Collections.singletonMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Validator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.FieldValidatorSource;
import org.apache.tapestry.services.ValidationMessagesSource;
import org.testng.annotations.Test;

public class FieldValidatorSourceImplTest extends InternalBaseTestCase
{
    public interface FieldComponent extends Field, Component
    {

    };

    @Test
    public void unknown_validator_type()
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator validator = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();
        Map<String, Validator> map = newMap();

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getLocale(resources, Locale.ENGLISH);
        train_getContainerMessages(resources, containerMessages);

        map.put("alpha", validator);
        map.put("beta", validator);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        try
        {
            source.createValidator(field, "foo", null);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unknown validator type 'foo'.  Configured validators are alpha, beta.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_no_constraint() throws Exception
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator validator = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = newMessages();
        MessageFormatter formatter = newMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getComponentResources(field, resources);

        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "fred-required", false);

        train_getLocale(resources, Locale.FRENCH);

        train_getValidationMessages(messagesSource, Locale.FRENCH, messages);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(messages, "key", formatter);

        train_invokeIfBlank(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        FieldValidator fieldValidator = source.createValidator(field, "required", null);

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void component_messages_overrides_validator_messages() throws Exception
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator validator = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = newMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getLocale(resources, Locale.ENGLISH);
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "fred-required", true);

        train_getMessageFormatter(containerMessages, "fred-required", formatter);

        train_invokeIfBlank(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        FieldValidator fieldValidator = source.createValidator(field, "required", null);

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void single_validator_via_specification() throws Exception
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator validator = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = newMessages();
        MessageFormatter formatter = newMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "fred-required", false);

        train_getLocale(resources, Locale.FRENCH);

        train_getValidationMessages(messagesSource, Locale.FRENCH, messages);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(messages, "key", formatter);

        train_invokeIfBlank(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        FieldValidator fieldValidator = source.createValidators(field, "required");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multiple_validators_via_specification() throws Exception
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator required = newValidator();
        Validator minLength = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = newMessages();
        MessageFormatter requiredFormatter = newMessageFormatter();
        MessageFormatter minLengthFormatter = newMessageFormatter();
        Object inputValue = "input value";
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();
        Integer fifteen = 15;

        Map<String, Validator> map = newMap();

        map.put("required", required);
        map.put("minLength", minLength);

        train_getConstraintType(required, null);
        train_getConstraintType(minLength, Integer.class);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "fred-required", false);

        train_getLocale(resources, Locale.FRENCH);

        train_getValidationMessages(messagesSource, Locale.FRENCH, messages);

        train_getMessageKey(required, "required");
        train_getMessageFormatter(messages, "required", requiredFormatter);

        train_contains(containerMessages, "fred-minLength", false);

        train_getMessageKey(minLength, "min-length");
        train_getMessageFormatter(messages, "min-length", minLengthFormatter);

        train_coerce(coercer, "15", Integer.class, fifteen);

        train_invokeIfBlank(required, true);
        train_getValueType(required, Object.class);
        required.validate(field, null, requiredFormatter, inputValue);

        train_invokeIfBlank(minLength, false);
        train_getValueType(minLength, String.class);
        minLength.validate(field, fifteen, minLengthFormatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        FieldValidator fieldValidator = source.createValidators(field, "required,minLength=15");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_constraint() throws Exception
    {
        ValidationMessagesSource messagesSource = newValidationMessagesSource();
        Validator validator = newValidator();
        TypeCoercer coercer = newTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = newMessages();
        MessageFormatter formatter = newMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = newComponentResources();
        Messages containerMessages = newMessages();
        Integer five = 5;

        Map<String, Validator> map = singletonMap("minLength", validator);

        train_getConstraintType(validator, Integer.class);

        train_coerce(coercer, "5", Integer.class, five);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "fred-minLength", false);

        train_getLocale(resources, Locale.FRENCH);

        train_getValidationMessages(messagesSource, Locale.FRENCH, messages);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(messages, "key", formatter);

        train_invokeIfBlank(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, five, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(messagesSource, coercer, map);

        FieldValidator fieldValidator = source.createValidator(field, "minLength", "5");

        fieldValidator.validate(inputValue);

        verify();
    }

    private FieldComponent newFieldComponent()
    {
        return newMock(FieldComponent.class);
    }

    private void test(String specification, ValidatorSpecification... expected)
    {
        List<ValidatorSpecification> specs = FieldValidatorSourceImpl.parse(specification);

        assertEquals(specs, Arrays.asList(expected));
    }

    @Test
    public void parse_simple_type_list()
    {
        test(
                "required,email",
                new ValidatorSpecification("required", null),
                new ValidatorSpecification("email", null));
    }

    @Test
    public void parse_single_type()
    {
        test("required", new ValidatorSpecification("required", null));
    }

    @Test
    public void ignore_whitespace_around_type_name()
    {
        test(
                "  required  ,  email  ",
                new ValidatorSpecification("required", null),
                new ValidatorSpecification("email", null));
    }

    @Test
    public void parse_simple_type_with_value()
    {
        test(
                "minLength=5,sameAs=otherComponentId",
                new ValidatorSpecification("minLength", "5"),
                new ValidatorSpecification("sameAs", "otherComponentId"));
    }

    @Test
    public void whitespace_ignored_around_value()
    {
        test("minLength=  5 , sameAs  = otherComponentId ", new ValidatorSpecification("minLength",
                "5"), new ValidatorSpecification("sameAs", "otherComponentId"));
    }

    @Test
    public void dangling_equals_sign_is_empty_string_value()
    {
        test("minLength=  ", new ValidatorSpecification("minLength", ""));
    }

    @Test
    public void unexpected_character_not_a_comma()
    {
        try
        {
            test("required.email");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unexpected character '.' at position 9 of input string: required.email");
        }
    }

    @Test
    public void unexpected_character_after_constraint_value()
    {
        try
        {
            test("minLength=3 . email");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unexpected character '.' at position 13 of input string: minLength=3 . email");
        }
    }
}
