// Copyright 2009, 2011 The Apache Software Foundation
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
package org.example.testapp.pages;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.testapp.services.Bar;

@Import(stack = "core")
public class InjectValidatorDemo 
{
	@NotNull(groups=Bar.class)
	@Validate("minlength=5")
	@Property
	@Persist
	private String userName;
	
	@Inject
	private Validator validator;
	
	@InjectComponent
	private Form form;
	
	void onValidateFromForm()
	{
		Set<ConstraintViolation<InjectValidatorDemo>> violations = validator.validate(this, Bar.class);
		
		for (ConstraintViolation<InjectValidatorDemo> next : violations) 
		{
			form.recordError("User Name "+next.getMessage());
		}
	}
	
}
