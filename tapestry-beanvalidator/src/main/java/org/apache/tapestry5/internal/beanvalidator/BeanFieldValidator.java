// Copyright 2010, 2012 The Apache Software Foundation
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
package org.apache.tapestry5.internal.beanvalidator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.beanvalidator.BeanValidatorGroupSource;
import org.apache.tapestry5.beanvalidator.ClientConstraintDescriptor;
import org.apache.tapestry5.beanvalidator.ClientConstraintDescriptorSource;
import org.apache.tapestry5.internal.BeanValidationContext;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.MessageInterpolator.Context;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;


public class BeanFieldValidator implements FieldValidator
{
    private final Field field;
    private final ValidatorFactory validatorFactory;
    private final BeanValidatorGroupSource beanValidationGroupSource;
    private final ClientConstraintDescriptorSource clientValidatorSource;
    private final FormSupport formSupport;
    private final Environment environment;

    public BeanFieldValidator(Field field,
                              ValidatorFactory validatorFactory,
                              BeanValidatorGroupSource beanValidationGroupSource,
                              ClientConstraintDescriptorSource clientValidatorSource,
                              FormSupport formSupport,
                              Environment environment)
    {
        this.field = field;
        this.validatorFactory = validatorFactory;
        this.beanValidationGroupSource = beanValidationGroupSource;
        this.clientValidatorSource = clientValidatorSource;
        this.formSupport = formSupport;
        this.environment = environment;
    }

    public boolean isRequired()
    {
        return false;
    }

    public void render(final MarkupWriter writer)
    {
        final BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
        {
            return;
        }

        final Validator validator = validatorFactory.getValidator();

        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(beanValidationContext.getBeanType());

        String currentProperty = beanValidationContext.getCurrentProperty();

        if (currentProperty == null) return;

        PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(currentProperty);

        if (propertyDescriptor == null) return;

        for (final ConstraintDescriptor<?> descriptor : propertyDescriptor.getConstraintDescriptors())
        {
            Class<? extends Annotation> annotationType = descriptor.getAnnotation().annotationType();

            ClientConstraintDescriptor clientConstraintDescriptor = clientValidatorSource.getConstraintDescriptor(annotationType);

            if (clientConstraintDescriptor == null)
            {
                continue;
            }

            String message = format("%s %s", field.getLabel(), interpolateMessage(descriptor));

            Map<String, Object> attributes = CollectionFactory.newMap();

            for (String attribute : clientConstraintDescriptor.getAttributes())
            {
                Object object = descriptor.getAttributes().get(attribute);

                if (object == null)
                {
                    throw new NullPointerException(
                            String.format("Attribute '%s' of %s is null but is required to apply client-side validation.",
                                    attribute, descriptor));
                }
                attributes.put(attribute, object);
            }

            clientConstraintDescriptor.applyClientValidation(writer, message, attributes);
        }
    }

    @SuppressWarnings("unchecked")
    public void validate(final Object value) throws ValidationException
    {

        final BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
        {
            return;
        }

        final Validator validator = validatorFactory.getValidator();

        String currentProperty = beanValidationContext.getCurrentProperty();

        if (currentProperty == null) return;

        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(beanValidationContext.getBeanType());

        PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(currentProperty);

        if (propertyDescriptor == null) return;

        final Set<ConstraintViolation<Object>> violations = validator.validateValue(
                (Class<Object>) beanValidationContext.getBeanType(), currentProperty,
                value, beanValidationGroupSource.get());

        if (violations.isEmpty())
        {
            return;
        }

        final StringBuilder builder = new StringBuilder();

        for (Iterator iterator = violations.iterator(); iterator.hasNext(); )
        {
            ConstraintViolation<?> violation = (ConstraintViolation<Object>) iterator.next();

            builder.append(format("%s %s", field.getLabel(), violation.getMessage()));

            if (iterator.hasNext())
                builder.append(", ");

        }

        throw new ValidationException(builder.toString());

    }

    private String interpolateMessage(final ConstraintDescriptor<?> descriptor)
    {
        String messageTemplate = (String) descriptor.getAttributes().get("message");

        MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

        return messageInterpolator.interpolate(messageTemplate, new Context()
        {

            public ConstraintDescriptor<?> getConstraintDescriptor()
            {
                return descriptor;
            }

            public Object getValidatedValue()
            {
                return null;
            }
        });
    }
}
