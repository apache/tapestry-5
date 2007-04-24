// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Validator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.MessageFormatter;
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
        Field field = newField();
        MessageFormatter formatter = newMessageFormatter();
        Validator validator = newValidator();

        train_invokeIfBlank(validator, false);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator);

        fv.validate(null);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void blank_value_skipped() throws Exception
    {
        Field field = newField();
        MessageFormatter formatter = newMessageFormatter();
        Validator validator = newValidator();

        train_invokeIfBlank(validator, false);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator);

        fv.validate("");

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void nonmatching_value_type_skipped() throws Exception
    {
        Field field = newField();
        MessageFormatter formatter = newMessageFormatter();
        Validator validator = newValidator();
        Integer value = 15;

        train_invokeIfBlank(validator, true);
        train_getValueType(validator, String.class);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator);

        fv.validate(value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void value_type_check_skipped_for_null_values() throws Exception
    {
        Field field = newField();
        MessageFormatter formatter = newMessageFormatter();
        Validator validator = newValidator();

        train_invokeIfBlank(validator, true);

        validator.validate(field, null, formatter, null);

        replay();

        FieldValidator fv = new FieldValidatorImpl(field, null, formatter, validator);

        fv.validate(null);

        verify();
    }
}
