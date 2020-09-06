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

import static java.util.Collections.singletonMap;
import static org.apache.tapestry5.commons.util.CollectionFactory.newMap;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.validator.ValidatorMacro;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class FieldValidatorSourceImplTest extends InternalBaseTestCase
{
    public interface FieldComponent extends Field, Component
    {

    }

    @Test
    public void unknown_validator_type()
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        Map<String, Validator> map = newMap();
        ValidatorMacro macro = mockValidatorMacro();

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_alwaysNull(macro);

        map.put("alpha", validator);
        map.put("beta", validator);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(null, coercer, null, map, macro);

        try
        {
            source.createValidator(field, "foo", null);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unknown validator type 'foo'. Configured validators are alpha, beta.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_no_constraint() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages globalMessages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();
        ValidatorMacro macro = mockValidatorMacro();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getFormValidationId(fs, "form");

        train_getComponentResources(field, resources);

        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_alwaysNull(macro);

        train_contains(containerMessages, "form-fred-required-message", false);
        train_contains(containerMessages, "fred-required-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidator(field, "required", null);

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void component_messages_overrides_validator_messages() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();
        ValidatorMacro macro = mockValidatorMacro();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getFormValidationId(fs, "form");

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_alwaysNull(macro);

        train_contains(containerMessages, "form-fred-required-message", false);
        train_contains(containerMessages, "fred-required-message", true);

        train_getMessageFormatter(containerMessages, "fred-required-message", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(null, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidator(field, "required", null);

        fieldValidator.validate(inputValue);

        verify();
    }

    @Test
    public void component_messages_overrides_validator_messages_per_form() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();
        ValidatorMacro macro = mockValidatorMacro();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getConstraintType(validator, null);

        train_getFormValidationId(fs, "form");

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_alwaysNull(macro);

        train_contains(containerMessages, "form-fred-required-message", true);

        train_getMessageFormatter(containerMessages, "form-fred-required-message", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(null, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidator(field, "required", null);

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void constraint_value_from_message_catalog_per() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        Messages globalMessages = mockMessages();
        FormSupport fs = mockFormSupport();

        Map<String, Validator> map = singletonMap("minlength", validator);

        train_getConstraintType(validator, Integer.class);

        train_getFormValidationId(fs, "myform");

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");

        train_contains(containerMessages, "myform-fred-minlength", false);
        train_contains(containerMessages, "fred-minlength", true);
        train_get(containerMessages, "fred-minlength", "5");

        train_coerce(coercer, "5", Integer.class, 5);

        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "myform-fred-minlength-message", false);
        train_contains(containerMessages, "fred-minlength-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, 5, formatter, inputValue);

        ValidatorMacro macro = mockValidatorMacro();
        train_alwaysNull(macro);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidators(field, "minlength");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void constraint_value_from_message_catalog_per_form() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages globalMessages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();

        Map<String, Validator> map = singletonMap("minlength", validator);

        train_getConstraintType(validator, Integer.class);

        train_getFormValidationId(fs, "myform");

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");

        train_contains(containerMessages, "myform-fred-minlength", true);
        train_get(containerMessages, "myform-fred-minlength", "5");

        train_coerce(coercer, "5", Integer.class, 5);

        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "myform-fred-minlength-message", false);
        train_contains(containerMessages, "fred-minlength-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, 5, formatter, inputValue);

        ValidatorMacro macro = mockValidatorMacro();
        train_alwaysNull(macro);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidators(field, "minlength");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void missing_field_validator_constraint() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();

        Map<String, Validator> map = singletonMap("minlength", validator);

        train_getConstraintType(validator, Integer.class);

        train_getFormValidationId(fs, "myform");

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_contains(containerMessages, "myform-fred-minlength", false);
        train_contains(containerMessages, "fred-minlength", false);

        ValidatorMacro macro = mockValidatorMacro();
        train_alwaysNull(macro);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(null, coercer, fs, map, macro);

        try
        {
            source.createValidators(field, "minlength");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Validator 'minlength' requires a validation constraint (of type java.lang.Integer) but none was provided. The constraint may be provided inside the @Validator annotation on the property, or in the associated component message catalog as key 'myform-fred-minlength' or key 'fred-minlength'.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void single_validator_via_specification() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        FormSupport fs = mockFormSupport();
        Messages globalMessages = mockMessages();

        Map<String, Validator> map = singletonMap("required", validator);

        train_getFormValidationId(fs, "myform");

        train_getConstraintType(validator, null);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_contains(containerMessages, "myform-fred-required-message", false);
        train_contains(containerMessages, "fred-required-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, null, formatter, inputValue);

        ValidatorMacro macro = mockValidatorMacro();
        train_alwaysNull(macro);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidators(field, "required");

        fieldValidator.validate(inputValue);

        verify();
    }

    private void train_alwaysNull(ValidatorMacro macro)
    {
        expect(macro.valueForMacro(EasyMock.isA(String.class))).andReturn(null).anyTimes();
    }

    private ValidatorMacro mockValidatorMacro()
    {
        return newMock(ValidatorMacro.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simple_macro_expansion() throws Exception
    {

        ValidatorMacro macro = mockValidatorMacro();
        expect(macro.valueForMacro("combo")).andReturn("required,minlength=5");
        expect(macro.valueForMacro("required")).andReturn(null);
        expect(macro.valueForMacro("minlength")).andReturn(null);

        replay();

        FieldValidatorSourceImpl source = new FieldValidatorSourceImpl(null, null, null, null, macro);

        List<ValidatorSpecification> specs = source.toValidatorSpecifications("combo");

        assertListsEquals(specs, new ValidatorSpecification("required"), new ValidatorSpecification("minlength", "5"));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void macros_can_not_have_constraints() throws Exception
    {

        ValidatorMacro macro = mockValidatorMacro();
        expect(macro.valueForMacro("combo")).andReturn("required,minlength=5");

        replay();

        FieldValidatorSourceImpl source = new FieldValidatorSourceImpl(null, null, null, null, macro);

        try
        {
            source.toValidatorSpecifications("combo=3");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                    "'combo' is a validator macro, not a validator, and can not have a constraint value.");
        }

        verify();
    }

    @Test
    public void recursive_macros_are_caught()
    {

        ValidatorMacro macro = mockValidatorMacro();
        expect(macro.valueForMacro("combo")).andReturn("required,combo");
        expect(macro.valueForMacro("required")).andReturn(null);
        expect(macro.valueForMacro("combo")).andReturn("required,combo");

        replay();

        FieldValidatorSourceImpl source = new FieldValidatorSourceImpl(null, null, null, null, macro);

        try
        {
            source.toValidatorSpecifications("combo");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Validator macro 'combo' appears more than once.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multiple_validators_via_specification() throws Exception
    {
        Validator required = mockValidator();
        Validator minLength = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        Messages messages = mockMessages();
        MessageFormatter requiredFormatter = mockMessageFormatter();
        MessageFormatter minLengthFormatter = mockMessageFormatter();
        Object inputValue = "input value";
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        Integer fifteen = 15;
        FormSupport fs = mockFormSupport();
        Messages globalMessages = mockMessages();

        Map<String, Validator> map = newMap();

        map.put("required", required);
        map.put("minLength", minLength);

        train_getFormValidationId(fs, "myform");

        train_getConstraintType(required, null);
        train_getConstraintType(minLength, Integer.class);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);
        train_contains(containerMessages, "myform-fred-required-message", false);
        train_contains(containerMessages, "fred-required-message", false);


        train_getMessageKey(required, "required");
        train_getMessageFormatter(globalMessages, "required", requiredFormatter);

        train_contains(containerMessages, "myform-fred-minLength-message", false);
        train_contains(containerMessages, "fred-minLength-message", false);

        train_getMessageKey(minLength, "min-length");
        train_getMessageFormatter(globalMessages, "min-length", minLengthFormatter);

        train_coerce(coercer, "15", Integer.class, fifteen);

        train_isRequired(required, true);
        train_getValueType(required, Object.class);
        required.validate(field, null, requiredFormatter, inputValue);

        train_isRequired(minLength, false);
        train_getValueType(minLength, String.class);
        minLength.validate(field, fifteen, minLengthFormatter, inputValue);

        ValidatorMacro macro = mockValidatorMacro();
        train_alwaysNull(macro);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidators(field, "required,minLength=15");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_constraint() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        Integer five = 5;
        FormSupport fs = mockFormSupport();
        ValidatorMacro macro = mockValidatorMacro();
        Messages globalMessages = mockMessages();

        Map<String, Validator> map = singletonMap("minLength", validator);

        train_getConstraintType(validator, Integer.class);

        train_getFormValidationId(fs, "myform");

        train_coerce(coercer, "5", Integer.class, five);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        train_alwaysNull(macro);

        train_contains(containerMessages, "myform-fred-minLength-message", false);
        train_contains(containerMessages, "fred-minLength-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, five, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidator(field, "minLength", "5");

        fieldValidator.validate(inputValue);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_constraint_and_macro() throws Exception
    {
        Validator validator = mockValidator();
        TypeCoercer coercer = mockTypeCoercer();
        FieldComponent field = newFieldComponent();
        MessageFormatter formatter = mockMessageFormatter();
        Object inputValue = new Object();
        ComponentResources resources = mockComponentResources();
        Messages containerMessages = mockMessages();
        Integer five = 5;
        FormSupport fs = mockFormSupport();
        ValidatorMacro macro = mockValidatorMacro();
        Messages globalMessages = mockMessages();

        Map<String, Validator> map = singletonMap("minLength", validator);

        train_getConstraintType(validator, Integer.class);

        train_getFormValidationId(fs, "myform");

        train_coerce(coercer, "77", Integer.class, five);

        train_getComponentResources(field, resources);
        train_getId(resources, "fred");
        train_getContainerMessages(resources, containerMessages);

        expect(macro.valueForMacro("foo-bar-baz")).andReturn("minLength=77");
        expect(macro.valueForMacro("minLength")).andReturn(null);

        train_contains(containerMessages, "myform-fred-minLength-message", false);
        train_contains(containerMessages, "fred-minLength-message", false);

        train_getMessageKey(validator, "key");
        train_getMessageFormatter(globalMessages, "key", formatter);

        train_isRequired(validator, false);
        train_getValueType(validator, Object.class);
        validator.validate(field, five, formatter, inputValue);

        replay();

        FieldValidatorSource source = new FieldValidatorSourceImpl(globalMessages, coercer, fs, map, macro);

        FieldValidator fieldValidator = source.createValidator(field, "foo-bar-baz", null);

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
        test("required,email", new ValidatorSpecification("required", null), new ValidatorSpecification("email", null));
    }

    @Test
    public void parse_single_type()
    {
        test("required", new ValidatorSpecification("required", null));
    }

    @Test
    public void ignore_whitespace_around_type_name()
    {
        test("  required  ,  email  ", new ValidatorSpecification("required", null), new ValidatorSpecification(
                "email", null));
    }

    @Test
    public void parse_simple_type_with_value()
    {
        test("minLength=5,sameAs=otherComponentId", new ValidatorSpecification("minLength", "5"),
                new ValidatorSpecification("sameAs", "otherComponentId"));
    }

    @Test
    public void whitespace_ignored_around_value()
    {
        test("minLength=  5 , sameAs  = otherComponentId ", new ValidatorSpecification("minLength", "5"),
                new ValidatorSpecification("sameAs", "otherComponentId"));
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
            assertEquals(ex.getMessage(), "Unexpected character '.' at position 9 of input string: required.email");
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
            assertEquals(ex.getMessage(),
                    "Unexpected character '.' at position 13 of input string: minLength=3 . email");
        }
    }
}
