// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.util.Locale;

import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.Messages;

/**
 * For a particular field, geenerates the default validation for the field, in accordance with a
 * number of factors and contributions.
 */
public interface FieldValidatorDefaultSource
{
    /**
     * Analyzes the property type and property annotations to determine the default set of
     * validations for the property, which are wrapped to form a {@link FieldValidator} for a field.
     * 
     * @param field
     * @param overrideId
     * @param overrideMessages
     * @param locale
     * @param propertyType
     * @param propertyAnnotations
     * @return
     */
    FieldValidator createDefaultValidator(Field field, String overrideId,
            Messages overrideMessages, Locale locale, Class propertyType,
            AnnotationProvider propertyAnnotations);
}
