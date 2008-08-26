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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.ValidationConstraintGenerator;

import java.util.List;
import java.util.Locale;

public class FieldValidatorDefaultSourceImpl implements FieldValidatorDefaultSource
{
    private final ValidationConstraintGenerator validationConstraintGenerator;

    private final FieldValidatorSource fieldValidatorSource;

    public FieldValidatorDefaultSourceImpl(
            ValidationConstraintGenerator validationConstraintGenerator,
            FieldValidatorSource fieldValidatorSource)
    {
        this.validationConstraintGenerator = validationConstraintGenerator;
        this.fieldValidatorSource = fieldValidatorSource;
    }

    public FieldValidator createDefaultValidator(Field field, String overrideId,
                                                 Messages overrideMessages, Locale locale, Class propertyType,
                                                 AnnotationProvider propertyAnnotations)
    {
        List<FieldValidator> validators = newList();

        for (String constraint : validationConstraintGenerator.buildConstraints(
                propertyType,
                propertyAnnotations))
        {
            int equalsx = constraint.indexOf('=');

            String validatorType = equalsx > 0 ? constraint.substring(0, equalsx) : constraint;
            String constraintValue = equalsx > 0 ? constraint.substring(equalsx + 1) : null;

            FieldValidator validator = fieldValidatorSource.createValidator(
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

    public FieldValidator createDefaultValidator(ComponentResources resources, String parameterName)
    {
        Class propertyType = resources.getBoundType(parameterName);

        if (propertyType == null) return null;

        Field field = (Field) resources.getComponent();

        return createDefaultValidator(field, resources.getId(), resources.getContainerMessages(), resources.getLocale(),
                                      propertyType, resources.getAnnotationProvider(parameterName));
    }
}
