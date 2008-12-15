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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.corelib.components.*;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.PropertyEditContext;
import org.apache.tapestry5.util.EnumSelectModel;
import org.apache.tapestry5.util.EnumValueEncoder;

/**
 * A page that exists to contain blocks used to edit different types of properties. The blocks on this page are
 * contributed into the {@link BeanBlockSource} service configuration.
 *
 * @see BeanBlockContribution
 * @see BeanEditForm
 */
public class PropertyEditBlocks
{
    @Environmental
    private PropertyEditContext context;

    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label",
                    "translate=prop:textFieldTranslator", "validate=prop:textFieldValidator",
                    "clientId=prop:context.propertyId", "annotationProvider=context"})
    private TextField textField;

    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label",
                    "translate=prop:numberFieldTranslator", "validate=prop:numberFieldValidator",
                    "clientId=prop:context.propertyId", "annotationProvider=context"})
    private TextField numberField;


    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label", "encoder=valueEncoderForProperty",
                    "model=selectModelForProperty", "validate=prop:selectValidator",
                    "clientId=prop:context.propertyId"})
    private Select select;

    @SuppressWarnings("unused")
    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label",
                    "clientId=prop:context.propertyId"})
    private Checkbox checkboxField;

    @SuppressWarnings("unused")
    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label", "clientId=prop:context.propertyid",
                    "validate=prop:dateFieldValidator"})
    private DateField dateField;

    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label",
                    "translate=prop:passwordFieldTranslator", "validate=prop:passwordFieldValidator",
                    "clientId=prop:context.propertyId", "annotationProvider=context"})
    private PasswordField passwordField;

    @Component(
            parameters = {"value=context.propertyValue", "label=prop:context.label",
                    "translate=prop:textAreaTranslator",
                    "validate=prop:textAreaValidator", "clientId=prop:context.propertyId",
                    "annotationProvider=context"})
    private TextArea textArea;


    public PropertyEditContext getContext()
    {
        return context;
    }


    public FieldTranslator getTextFieldTranslator()
    {
        return context.getTranslator(textField);
    }

    public FieldValidator getTextFieldValidator()
    {
        return context.getValidator(textField);
    }

    public FieldTranslator getNumberFieldTranslator()
    {
        return context.getTranslator(numberField);
    }

    public FieldValidator getNumberFieldValidator()
    {
        return context.getValidator(numberField);
    }

    public FieldTranslator getPasswordFieldTranslator()
    {
        return context.getTranslator(passwordField);
    }

    public FieldValidator getPasswordFieldValidator()
    {
        return context.getValidator(passwordField);
    }

    public FieldTranslator getTextAreaTranslator()
    {
        return context.getTranslator(textArea);
    }

    public FieldValidator getTextAreaValidator()
    {
        return context.getValidator(textArea);
    }


    public FieldValidator getDateFieldValidator()
    {
        return context.getValidator(dateField);
    }

    public FieldValidator getSelectValidator()
    {
        return context.getValidator(select);
    }

    /**
     * Provide a value encoder for an enum type.
     */
    @SuppressWarnings("unchecked")
    public ValueEncoder getValueEncoderForProperty()
    {
        return new EnumValueEncoder(context.getPropertyType());
    }

    /**
     * Provide a select mode for an enum type.
     */
    @SuppressWarnings("unchecked")
    public SelectModel getSelectModelForProperty()
    {
        return new EnumSelectModel(context.getPropertyType(), context.getContainerMessages());
    }
}
