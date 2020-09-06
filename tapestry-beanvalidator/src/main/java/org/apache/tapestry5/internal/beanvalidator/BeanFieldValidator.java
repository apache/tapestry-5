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
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.BeanValidationContext;
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

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public void render(final MarkupWriter writer)
    {
        final BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
        {
            return;
        }

        final Validator validator = validatorFactory.getValidator();

        final String currentProperty = beanValidationContext.getCurrentProperty();

        if (currentProperty == null) return;
        
        final ValidationInfo validationInfo = getValidationInfo(beanValidationContext, currentProperty, validator);
        final PropertyDescriptor propertyDescriptor = validationInfo.getPropertyDescriptor();

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

    @Override
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
        
        final ValidationInfo validationInfo = getValidationInfo(beanValidationContext, currentProperty, validator);
        final PropertyDescriptor propertyDescriptor = validationInfo.getPropertyDescriptor();

        if (propertyDescriptor == null) return;

        final Set<ConstraintViolation<Object>> violations = validator.validateValue(
                (Class<Object>) validationInfo.getBeanType(), validationInfo.getPropertyName(),
                value, beanValidationGroupSource.get());

        if (violations.isEmpty())
        {
            return;
        }

        final StringBuilder builder = new StringBuilder();

        for (Iterator<ConstraintViolation<Object>> iterator = violations.iterator(); iterator.hasNext(); )
        {
            ConstraintViolation<?> violation = iterator.next();

            builder.append(format("%s %s", field.getLabel(), violation.getMessage()));

            if (iterator.hasNext())
                builder.append(", ");

        }

        throw new ValidationException(builder.toString());

    }

    /**
     * Returns the class of a given property, but only if it is a constrained property of the
     * parent class. Otherwise, it returns null.
     */
    final private static Class<?> getConstrainedPropertyClass(BeanDescriptor beanDescriptor, String propertyName)
    {
        Class<?> clasz = null;
        for (PropertyDescriptor descriptor : beanDescriptor.getConstrainedProperties()) 
        {
            if (descriptor.getPropertyName().equals(propertyName)) 
            {
                clasz = descriptor.getElementClass();
                break;
            }
        }
        return clasz;
    }

    private String interpolateMessage(final ConstraintDescriptor<?> descriptor)
    {
        String messageTemplate = (String) descriptor.getAttributes().get("message");

        MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

        return messageInterpolator.interpolate(messageTemplate, new Context()
        {

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor()
            {
                return descriptor;
            }

            @Override
            public Object getValidatedValue()
            {
                return null;
            }
        });
    }
    
    final private static ValidationInfo getValidationInfo(BeanValidationContext beanValidationContext, String currentProperty, Validator validator) {
        Class<?> beanType = beanValidationContext.getBeanType();
        String[] path = currentProperty.split("\\.");
        BeanDescriptor beanDescriptor = validator.getConstraintsForClass(beanType);
        
        for (int i = 1; i < path.length - 1; i++) 
        {
            Class<?> constrainedPropertyClass = getConstrainedPropertyClass(beanDescriptor, path[i]);
            if (constrainedPropertyClass != null) {
                beanType = constrainedPropertyClass;
                beanDescriptor = validator.getConstraintsForClass(beanType);
            }
        }

        final String propertyName = path[path.length - 1];
        PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(propertyName);
        return new ValidationInfo(beanType, propertyName, propertyDescriptor);
    }
    
    final private static class ValidationInfo {
        final private Class<?> beanType;
        final private String propertyName;
        final private PropertyDescriptor propertyDescriptor;
        public ValidationInfo(Class<?> beanType, String propertyName,
                PropertyDescriptor propertyDescriptor) 
        {
            super();
            this.beanType = beanType;
            this.propertyName = propertyName;
            this.propertyDescriptor = propertyDescriptor;
        }
        
        public Class<?> getBeanType() 
        {
            return beanType;
        }
        
        public String getPropertyName() 
        {
            return propertyName;
        }

        public PropertyDescriptor getPropertyDescriptor() 
        {
            return propertyDescriptor;
        }

    }
    
}
