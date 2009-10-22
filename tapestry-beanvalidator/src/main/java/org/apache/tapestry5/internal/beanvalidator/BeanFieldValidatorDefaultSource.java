// Copyright 2009 The Apache Software Foundation
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

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.beanvalidator.BeanValidatorGroupSource;
import org.apache.tapestry5.beanvalidator.BeanValidatorSource;
import org.apache.tapestry5.internal.services.CompositeFieldValidator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.BeanValidationContext;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;

public class BeanFieldValidatorDefaultSource implements FieldValidatorDefaultSource 
{
	private final FieldValidatorDefaultSource fieldValidatorDefaultSource;
	private final BeanValidatorSource beanValidatorSource;
	private final BeanValidatorGroupSource beanValidationGroupSource;
	private final Environment environment;

	public BeanFieldValidatorDefaultSource(
			@Core FieldValidatorDefaultSource fieldValidatorDefaultSource,
			final BeanValidatorSource beanValidatorSource,
			final BeanValidatorGroupSource beanValidationGroupSource,
			final Environment environment) 
	{
		this.fieldValidatorDefaultSource = fieldValidatorDefaultSource;
		this.beanValidatorSource = beanValidatorSource;
		this.beanValidationGroupSource = beanValidationGroupSource;
		this.environment = environment;
	}

	public FieldValidator createDefaultValidator(final Field field,
			final String overrideId, final Messages overrideMessages,
			final Locale locale, final Class propertyType,
			final AnnotationProvider propertyAnnotations) 
	{
		
		FieldValidator validator = fieldValidatorDefaultSource.createDefaultValidator(
				field, overrideId, overrideMessages, locale, propertyType, propertyAnnotations);

		FieldValidator beanValidator = new FieldValidator() {
			
			public boolean isRequired() 
			{
				return false;
			}

			public void render(final MarkupWriter writer) 
			{
			}

			public void validate(final Object value) throws ValidationException 
			{

				final BeanValidationContext beanValidationContext = BeanFieldValidatorDefaultSource.this.environment
						.peek(BeanValidationContext.class);

				if (beanValidationContext == null) 
				{
					return;
				}
				
				final Validator validator = BeanFieldValidatorDefaultSource.this.beanValidatorSource.create();
				
				final Set<ConstraintViolation<Object>> violations = validator.validateValue(
								(Class<Object>) beanValidationContext.getObject().getClass(), overrideId, 
								value, beanValidationGroupSource.get());
				
				if (violations.isEmpty()) 
				{
					return;
				}
				
				final StringBuilder builder = new StringBuilder();
				
				for (Iterator iterator = violations.iterator(); iterator.hasNext();) 
				{
					ConstraintViolation<?> violation = (ConstraintViolation<Object>) iterator.next();
					
					builder.append(format("%s %s", field.getLabel(), violation.getMessage()));
					
					if(iterator.hasNext())
						builder.append(", ");
			
				}
				
				throw new ValidationException(builder.toString());

			}

		};
		
		return new CompositeFieldValidator(Arrays.asList(validator, beanValidator));
	}

	public FieldValidator createDefaultValidator(
			final ComponentResources resources, final String parameterName) 
	{
		final Class propertyType = resources.getBoundType(parameterName);
		
		if (propertyType == null)
			return null;

		final Field field = (Field) resources.getComponent();

		return createDefaultValidator(field, resources.getId(), resources
				.getContainerMessages(), resources.getLocale(), propertyType,
				resources.getAnnotationProvider(parameterName));
	}
}