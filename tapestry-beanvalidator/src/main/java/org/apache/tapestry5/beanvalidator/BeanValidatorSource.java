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

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Creates {@link jakarta.validation.ValidatorFactory}.
 * 
 * @since 5.2.0.0
 */
@UsesOrderedConfiguration(BeanValidatorConfigurer.class)
public interface BeanValidatorSource 
{

	/**
	 * Creates a new Validator.
	 */
	Validator create();

	/**
	 * Returns the ValidatorFactory from which Validators are created.
	 */
	ValidatorFactory getValidatorFactory();
}
