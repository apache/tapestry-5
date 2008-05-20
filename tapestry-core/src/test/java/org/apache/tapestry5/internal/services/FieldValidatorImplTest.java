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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.testng.annotations.Test;

/**
 * Tests a few outlier cases not covered by {@link FieldValidatorSourceImplTest}.
 */
public class FieldValidatorImplTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void null_value_skipped() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Validator validator = mockValidator();

        train_isRequired(validator, false);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator, null);

        fv.validate(null);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void blank_value_skipped() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Validator validator = mockValidator();

        train_isRequired(validator, false);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator, null);

        fv.validate("");

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void nonmatching_value_type_skipped() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Validator validator = mockValidator();
        Integer value = 15;

        train_isRequired(validator, true);
        train_getValueType(validator, String.class);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator, null);

        fv.validate(value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void value_type_check_skipped_for_null_values() throws Exception
    {
        Field field = mockField();
        MessageFormatter formatter = mockMessageFormatter();
        Validator validator = mockValidator();

        train_isRequired(validator, true);

        validator.validate(field, null, formatter, null);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator, null);

        fv.validate(null);

        verify();
    }
}
