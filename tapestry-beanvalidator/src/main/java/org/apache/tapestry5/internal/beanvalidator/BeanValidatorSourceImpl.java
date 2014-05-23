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

import java.util.List;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.tapestry5.beanvalidator.BeanValidatorConfigurer;
import org.apache.tapestry5.beanvalidator.BeanValidatorSource;

public class BeanValidatorSourceImpl implements BeanValidatorSource 
{
	private final ValidatorFactory validatorFactory;

	public BeanValidatorSourceImpl(final List<BeanValidatorConfigurer> contribution) 
	{
		final Configuration<?> configuration = Validation.byDefaultProvider().configure();

		for (final BeanValidatorConfigurer configurer : contribution) 
		{
			configurer.configure(configuration);
		}

		this.validatorFactory = configuration.buildValidatorFactory();
	}

	@Override
	public Validator create() 
	{
		return this.validatorFactory.getValidator();
	}

	@Override
	public ValidatorFactory getValidatorFactory() 
	{
		return this.validatorFactory;
	}

}