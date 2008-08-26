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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;

import java.util.Locale;

/**
 * For a particular field, generates the default validation for the field, in accordance with a number of factors and
 * contributions.
 */
public interface FieldValidatorDefaultSource
{
    /**
     * Analyzes the property type and property annotations to determine the default set of validations for the property,
     * which are wrapped to form a {@link org.apache.tapestry5.FieldValidator} for a field.
     *
     * @param field               Field component for which a validator is being created
     * @param overrideId          the id of the component, used to locate related messages for labels and errors
     * @param overrideMessages    where to search for label and error messages
     * @param locale              locale used for locating messages
     * @param propertyType        type of property bound to the editting parameter of the field (typically, the
     *                            parameter named "value").
     * @param propertyAnnotations source of annotations for the property being editted
     * @return a validator reflecting all default validations for the field
     */
    FieldValidator createDefaultValidator(Field field, String overrideId, Messages overrideMessages, Locale locale,
                                          Class propertyType, AnnotationProvider propertyAnnotations);

    /**
     * A convienience for the full version; assumes that the resources are associated with a {@link
     * org.apache.tapestry5.Field}.
     *
     * @param resources
     * @param parameterName
     * @return
     */
    FieldValidator createDefaultValidator(ComponentResources resources, String parameterName);
}
