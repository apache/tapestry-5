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

import java.util.Locale;

import org.apache.tapestry.AnnotationProvider;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FieldValidatorSource;
import org.apache.tapestry.services.ValidationConstraintGenerator;
import org.testng.annotations.Test;

public class FieldValidatorDefaultSourceImplTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void invokes_all_constraint_generators() throws Exception
    {
        getMocksControl().checkOrder(true);

        ValidationConstraintGenerator gen = newValidationConstraintGenerator();
        FieldValidator fv1 = newFieldValidator();
        FieldValidator fv2 = newFieldValidator();
        FieldValidatorSource source = newFieldValidatorSource();
        Class propertyType = Integer.class;
        AnnotationProvider provider = newAnnotationProvider();
        String overrideId = "overrideId";
        Messages overrideMessages = newMessages();
        Field field = newField();
        Locale locale = Locale.ENGLISH;
        String value = "*VALUE*";

        train_buildConstraints(gen, propertyType, provider, "cons1", "cons2");

        train_createValidator(
                source,
                field,
                "cons1",
                null,
                overrideId,
                overrideMessages,
                locale,
                fv1);

        train_createValidator(
                source,
                field,
                "cons2",
                null,
                overrideId,
                overrideMessages,
                locale,
                fv2);

        fv1.validate(value);
        fv2.validate(value);

        replay();

        FieldValidatorDefaultSource fieldValidatorSource = new FieldValidatorDefaultSourceImpl(gen,
                source);

        FieldValidator composite = fieldValidatorSource.createDefaultValidator(
                field,
                overrideId,
                overrideMessages,
                locale,
                propertyType,
                provider);

        composite.validate(value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validator_with_constraint() throws Exception
    {
        ValidationConstraintGenerator gen = newValidationConstraintGenerator();
        FieldValidator fv = newFieldValidator();
        FieldValidatorSource source = newFieldValidatorSource();
        Class propertyType = Integer.class;
        AnnotationProvider provider = newAnnotationProvider();
        String overrideId = "overrideId";
        Messages overrideMessages = newMessages();
        Field field = newField();
        Locale locale = Locale.ENGLISH;

        train_buildConstraints(gen, propertyType, provider, "foo=bar");

        train_createValidator(source, field, "foo", "bar", overrideId, overrideMessages, locale, fv);

        replay();

        FieldValidatorDefaultSource fieldValidatorSource = new FieldValidatorDefaultSourceImpl(gen,
                source);

        FieldValidator composite = fieldValidatorSource.createDefaultValidator(
                field,
                overrideId,
                overrideMessages,
                locale,
                propertyType,
                provider);

        assertSame(composite, fv);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void no_validators_at_all() throws Exception
    {
        ValidationConstraintGenerator gen = newValidationConstraintGenerator();
        FieldValidatorSource source = newFieldValidatorSource();
        Class propertyType = Integer.class;
        AnnotationProvider provider = newAnnotationProvider();
        String overrideId = "overrideId";
        Messages overrideMessages = newMessages();
        Field field = newField();
        Locale locale = Locale.ENGLISH;
        String value = "*VALUE*";

        train_buildConstraints(gen, propertyType, provider);

        replay();

        FieldValidatorDefaultSource fieldValidatorSource = new FieldValidatorDefaultSourceImpl(gen,
                source);

        FieldValidator composite = fieldValidatorSource.createDefaultValidator(
                field,
                overrideId,
                overrideMessages,
                locale,
                propertyType,
                provider);

        composite.validate(value);

        verify();
    }
}
