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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;

/**
 * Defines a context for editing a property of a bean via {@link org.apache.tapestry5.corelib.components.BeanEditor}.
 * This value is made available to blocks via the {@link org.apache.tapestry5.annotations.Environmental} annotation.
 *
 * @see org.apache.tapestry5.services.BeanBlockSource
 */
public interface PropertyEditContext extends AnnotationProvider
{
    /**
     * Returns the current value of the property being edited (the context encapsulates the object containing the
     * property).
     */
    Object getPropertyValue();

    /**
     * Updates the value of the property being edited (the context encapsulates the object containing the property).
     *
     * @param value new value for the property
     */
    void setPropertyValue(Object value);

    /**
     * Returns the user-presentable label, for use with the {@link org.apache.tapestry5.corelib.components.Label}
     * component, or to be integrated into any validation error messages.
     */
    String getLabel();

    /**
     * Returns the translator appropriate for the field (this is based on the property type).
     *
     * @param field
     * @see org.apache.tapestry5.services.TranslatorSource
     */
    FieldTranslator getTranslator(Field field);

    /**
     * Returns the FieldValidator for the field.
     *
     * @see org.apache.tapestry5.beaneditor.Validate
     * @see org.apache.tapestry5.services.FieldValidatorDefaultSource
     */
    FieldValidator getValidator(Field field);

    /**
     * Returns a string that identifies the property, usually the property name. This is used as the basis for the
     * client-side client id.
     */
    String getPropertyId();

    /**
     * Returns the type of the property being edited.
     */
    Class getPropertyType();

    /**
     * Returns the message catalog for the container of the {@link org.apache.tapestry5.corelib.components.BeanEditForm},
     * which is the correct place to look for strings used for labels, etc.
     */
    Messages getContainerMessages();
}
