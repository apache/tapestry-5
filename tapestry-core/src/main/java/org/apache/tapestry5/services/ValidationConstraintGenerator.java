// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

import java.util.List;

/**
 * Invoked to generate a list of validation constraint strings for a property. This typically involves scanning the
 * property for annotations or naming conventions that confer the desired validation. The constraint strings are
 * ultimately handed to {@link FieldValidatorSource#createValidator(org.apache.tapestry5.Field, String, String, String,
 * org.apache.tapestry5.ioc.Messages, java.util.Locale)}.
 */
@UsesOrderedConfiguration(ValidationConstraintGenerator.class)
public interface ValidationConstraintGenerator
{
    /**
     * For a given property, identify all the approprite validation constraints. Each returned value is the name of a
     * validator (i.e., "required") or a validator name and configuration (i.e., "minlength=5"). These contraints are
     * exactly the individual terms in a {@link FieldValidatorSource#createValidators(org.apache.tapestry5.Field,
     * String) validate specification}. These will ultimately be used to create {@link FieldValidator}s for the field
     * that edits the property.
     *
     * @param propertyType       the type of the property for which constraints are needed
     * @param annotationProvider provides access to any annotations concerning the property (for implementations that
     *                           are based on analysis of property annotations)
     * @return a list of constraints
     * @see FieldValidatorSource
     */
    List<String> buildConstraints(Class propertyType, AnnotationProvider annotationProvider);
}
