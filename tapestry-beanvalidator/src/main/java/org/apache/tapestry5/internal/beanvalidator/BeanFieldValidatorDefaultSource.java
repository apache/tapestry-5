// Copyright 2009, 2010 The Apache Software Foundation
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

import java.util.Arrays;
import java.util.Locale;

import javax.validation.ValidatorFactory;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.beanvalidator.BeanValidatorGroupSource;
import org.apache.tapestry5.beanvalidator.ClientConstraintDescriptorSource;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.internal.services.CompositeFieldValidator;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FormSupport;

public class BeanFieldValidatorDefaultSource implements FieldValidatorDefaultSource 
{
	private final FieldValidatorDefaultSource fieldValidatorDefaultSource;
	private final ValidatorFactory validatorFactory;
	private final BeanValidatorGroupSource beanValidationGroupSource;
	private final ClientConstraintDescriptorSource clientValidatorSource;
	private final FormSupport formSupport;
	private final Environment environment;

	public BeanFieldValidatorDefaultSource(
			@Core FieldValidatorDefaultSource fieldValidatorDefaultSource,
			final ValidatorFactory validatorFactory,
			final BeanValidatorGroupSource beanValidationGroupSource,
			final ClientConstraintDescriptorSource clientValidatorSource,
			final FormSupport formSupport,
			final Environment environment) 
	{
		this.fieldValidatorDefaultSource = fieldValidatorDefaultSource;
		this.validatorFactory = validatorFactory;
		this.beanValidationGroupSource = beanValidationGroupSource;
		this.clientValidatorSource = clientValidatorSource;
		this.formSupport = formSupport;
		this.environment = environment;
	}

	@Override
	public FieldValidator createDefaultValidator(final Field field,
			final String overrideId, final Messages overrideMessages,
			final Locale locale, final Class propertyType,
			final AnnotationProvider propertyAnnotations) 
	{
		
		FieldValidator validator = fieldValidatorDefaultSource.createDefaultValidator(
				field, overrideId, overrideMessages, locale, propertyType, propertyAnnotations);

		
		FieldValidator beanValidator 
			= new BeanFieldValidator(field, validatorFactory, beanValidationGroupSource, 
					clientValidatorSource, formSupport, environment);
		
		return new CompositeFieldValidator(Arrays.asList(validator, beanValidator));
	}

	@Override
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