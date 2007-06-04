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

import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Translator;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.beaneditor.Validate;
import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.corelib.components.Label;
import org.apache.tapestry.ioc.Messages;

/**
 * Defines a context for editting a property of a bean via {@link BeanEditForm}. This value is made
 * available to blocks via the {@link Environmental} annotation.
 * 
 * @see BeanBlockSource
 */
public interface PropertyEditContext
{
    /**
     * Returns the current value of the property being editted (the context encapsulates the object
     * containing the property).
     */
    Object getPropertyValue();

    /**
     * Updates the value of the property being editted (the context encapsulates the object
     * containing the property).
     * 
     * @param value
     *            new value for the property
     */
    void setPropertyValue(Object value);

    /**
     * Returns the user-presentable label, for use with the {@link Label} component, or to be
     * integrated into any validation error messages.
     */
    String getLabel();

    /**
     * Returns the translator appropriate for the field (this is based on the property type).
     * 
     * @see TranslatorDefaultSource
     */
    Translator getTranslator();

    /**
     * Returns the FieldValidator for the field.
     * 
     * @see Validate
     * @see FieldValidatorDefaultSource
     */
    FieldValidator getValidator(Field field);

    /**
     * Returns a string that identifies the property, usually the property name. This is used as the
     * basis for the client-side client id.
     */
    String getPropertyId();

    /**
     * Returns the type of the property being editted.
     */
    Class getPropertyType();

    /**
     * Returns the message catalog for the container of the {@link BeanEditForm}, which is the
     * correct place to look for strings used for labels, etc.
     */
    Messages getContainerMessages();
}
