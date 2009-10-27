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
package org.apache.tapestry5.beanvalidator;

import java.util.Locale;

import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.apache.tapestry5.internal.beanvalidator.BeanFieldValidatorDefaultSource;
import org.apache.tapestry5.internal.beanvalidator.BeanValidationGroupSourceImpl;
import org.apache.tapestry5.internal.beanvalidator.BeanValidatorSourceImpl;
import org.apache.tapestry5.internal.beanvalidator.MessageInterpolatorImpl;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;

/**
 * Module for JSR-303 services.
 * 
 * @since 5.2.0.0
 */
public class BeanValidatorModule 
{
	public static void bind(final ServiceBinder binder) 
	{
		binder.bind(FieldValidatorDefaultSource.class, BeanFieldValidatorDefaultSource.class)
					.withId("BeanFieldValidatorDefaultSource");
		binder.bind(BeanValidatorGroupSource.class, BeanValidationGroupSourceImpl.class);
		binder.bind(BeanValidatorSource.class, BeanValidatorSourceImpl.class);
	}

	public static void contributeServiceOverride(
			MappedConfiguration<Class, Object> configuration,
			@Local FieldValidatorDefaultSource source) 
	{
		configuration.add(FieldValidatorDefaultSource.class, source);
	}
	
	public static Validator buildBeanValidator(ValidatorFactory validatorFactory, PropertyShadowBuilder propertyShadowBuilder) 
	{
		return propertyShadowBuilder.build(validatorFactory, "validator", Validator.class);
	}
	
	
	public static ValidatorFactory buildValidatorFactory( BeanValidatorSource beanValidatorSource, PropertyShadowBuilder propertyShadowBuilder) 
	{
		return propertyShadowBuilder.build(beanValidatorSource, "validatorFactory", ValidatorFactory.class);
	}

	public static void contributeBeanValidatorGroupSource(
			final Configuration<Class> configuration) 
	{
		configuration.add(Default.class);
	}
	
	public static void contributeBeanValidatorSource(
			final OrderedConfiguration<BeanValidatorConfigurer> configuration, final ThreadLocale threadLocale) 
	{
		configuration.add("LocaleAwareMessageInterpolator", new BeanValidatorConfigurer() 
		{
			public void configure(javax.validation.Configuration<?> configuration) 
			{
				MessageInterpolator defaultInterpolator = configuration.getDefaultMessageInterpolator();
				
				configuration.messageInterpolator(new MessageInterpolatorImpl(defaultInterpolator, threadLocale));
			}
		});
	}

}
