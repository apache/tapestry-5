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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FieldValidatorSource;
import org.apache.tapestry.services.ValidationConstraintGenerator;

public class FieldValidatorDefaultSourceImpl implements FieldValidatorDefaultSource
{
    private final ValidationConstraintGenerator _validationConstraintGenerator;

    private final FieldValidatorSource _fieldValidatorSource;

    public FieldValidatorDefaultSourceImpl(
            ValidationConstraintGenerator validationConstraintGenerator,
            FieldValidatorSource fieldValidatorSource)
    {
        _validationConstraintGenerator = validationConstraintGenerator;
        _fieldValidatorSource = fieldValidatorSource;
    }

    public FieldValidator createDefaultValidator(Field field, String overrideId,
            Messages overrideMessages, Locale locale, Class propertyType,
            AnnotationProvider propertyAnnotations)
    {
        List<FieldValidator> validators = newList();

        for (String constraint : _validationConstraintGenerator.buildConstraints(
                propertyType,
                propertyAnnotations))
        {
            int equalsx = constraint.indexOf('=');

            String validatorType = equalsx > 0 ? constraint.substring(0, equalsx) : constraint;
            String constraintValue = equalsx > 0 ? constraint.substring(equalsx + 1) : null;

            FieldValidator validator = _fieldValidatorSource.createValidator(
                    field,
                    validatorType,
                    constraintValue,
                    overrideId,
                    overrideMessages,
                    locale);

            validators.add(validator);
        }

        return validators.size() == 1 ? validators.get(0) : new CompositeFieldValidator(validators);
    }
}
