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

import jakarta.validation.Configuration;

/**
 * Defines the interface for a chain-of-command that updates JSR-303 configuration in some way before the {@link
 * jakarta.validation.ValidatorFactory} is created.
 * 
 * @since 5.2.0.0
 */
public interface BeanValidatorConfigurer 
{
	/**
	 * Passed the configuration so as to make changes.
	 */
	void configure(Configuration<?> configuration);
}
