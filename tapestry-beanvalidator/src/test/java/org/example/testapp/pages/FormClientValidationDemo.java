// Copyright 2010 The Apache Software Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.example.testapp.services.Foo;

@Import(stack = "core")
public class FormClientValidationDemo
{
	@NotNull(groups=Foo.class)
	@Pattern(regexp="[a-zA-Z]+")
	@Validate("minlength=5")
	@Property
	@Persist
	private String userName;
	
	@NotNull
	@Property
	@Persist
	private String password;
	
	@NotNull
	@Size(min=2, max=3)
	@Property
	@Persist
	private Collection<String> languages;
	
	@NotNull
	@Property
	@Persist
	private String color; 
	
	@NotNull
	@Property
	@Persist
	private Date date; 

    void onPrepare() {
        if (languages == null) {
            languages = new ArrayList<String>();
        }
    }

	public StringValueEncoder getStringValueEncoder()
	{
		return new StringValueEncoder();
	}

}
