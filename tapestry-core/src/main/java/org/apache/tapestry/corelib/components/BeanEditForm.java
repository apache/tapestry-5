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

package org.apache.tapestry.corelib.components;

import java.util.Locale;

import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.EnumSelectModel;
import org.apache.tapestry.EnumValueEncoder;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.BeanEditorModel;
import org.apache.tapestry.beaneditor.PropertyEditModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.BeanEditorModelSource;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.TranslatorDefaultSource;

/**
 * A component that creates an entire form editting the properties of a particular bean. Inspired by
 * <a href="http://www.trailsframework.org/">Trails</a> and <a
 * href="http://beanform.sourceforge.net/">BeanForm</a> (both for Tapestry 4). Generates a simple
 * UI for editting the properties of a JavaBean, with the flavor of UI for each property (text
 * field, checkbox, drop down list) determined from the property type, and the order and validation
 * for the properties determined from annotations on the property's getter and setter methods.
 * <p>
 * This component is likely to change more than any other thing in Tapestry! What's available now is
 * a very limited preview of its eventual functionality.
 * 
 * @see BeanEditorModel
 * @see BeanEditorModelSource
 */
@ComponentClass
public class BeanEditForm
{
    /** The object to be editted by the BeanEditor. */
    @Parameter(required = true)
    private Object _object;

    @Inject
    private ComponentResources _resources;

    @Inject("infrastructure:BeanEditorModelSource")
    private BeanEditorModelSource _modelSource;

    @Inject("infrastructure:TranslatorDefaultSource")
    private TranslatorDefaultSource _translatorDefaultSource;

    @Inject("infrastructure:FieldValidatorDefaultSource")
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private Block _text;

    @Inject
    private Block _enum;

    @Inject
    private Block _checkbox;

    @Component
    private Form _form;

    @Component(parameters =
    { "value=valueForProperty", "label=prop:propertyEditModel.label",
            "encoder=valueEncoderForProperty", "model=selectModelForProperty",
            "validate=prop:validateForProperty", "clientId=prop:propertyName" })
    private Select _select;

    @Component(parameters =
    { "value=valueForProperty", "label=prop:propertyEditModel.label",
            "translate=prop:translateForProperty", "validate=prop:validateForProperty",
            "clientId=prop:propertyName" })
    private TextField _textField;

    @Component(parameters =
    { "value=valueForProperty", "label=prop:propertyEditModel.label", "clientId=prop:propertyName" })
    private Checkbox _checkboxField;

    @Inject
    private Messages _messages;

    @Inject
    private Locale _locale;

    @Parameter
    private BeanEditorModel _model;

    // Values that change with each change to the current property:

    private String _propertyName;

    private PropertyEditModel _propertyEditModel;

    private Block _blockForProperty;

    private Field _fieldForProperty;

    public BeanEditorModel getModel()
    {
        return _model;
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyEditModel = _model.get(propertyName);

        _blockForProperty = null;
        _fieldForProperty = null;

        _propertyName = propertyName;

        String editorType = _propertyEditModel.getEditorType();

        if (editorType.equals("text"))
        {
            _blockForProperty = _text;
            _fieldForProperty = _textField;
            return;
        }

        if (editorType.equals("enum"))
        {
            _blockForProperty = _enum;
            _fieldForProperty = _select;
            return;
        }

        if (editorType.equals("checkbox"))
        {
            _blockForProperty = _checkbox;
            _fieldForProperty = _checkboxField;
            return;
        }

        throw new IllegalArgumentException(_messages.format("no-editor", editorType, propertyName));
    }

    boolean onPrepareFromForm()
    {
        // Fire a new prepare event to be consumed by the container. This is the container's
        // chance to ensure that there's an object to edit.

        _resources.triggerEvent(Form.PREPARE, null, null);

        if (_model == null)
        {
            Class<? extends Object> beanType = _object.getClass();

            _model = _modelSource.create(beanType, _resources.getContainerResources());
        }

        return true; // abort the form's prepare event
    }

    public Translator getTranslateForProperty()
    {
        return _translatorDefaultSource.find(_propertyEditModel.getPropertyType());
    }

    public FieldValidator getValidateForProperty()
    {
        return _fieldValidatorDefaultSource.createDefaultValidator(
                _fieldForProperty,
                _propertyName,
                _resources.getContainerMessages(),
                _locale,
                _propertyEditModel.getPropertyType(),
                _propertyEditModel.getConduit());
    }

    public PropertyEditModel getPropertyEditModel()
    {
        return _propertyEditModel;
    }

    public Block getBlockForProperty()
    {
        return _blockForProperty;
    }

    public Field getFieldForProperty()
    {
        return _fieldForProperty;
    }

    public Object getValueForProperty()
    {
        return _propertyEditModel.getConduit().get(_object);
    }

    public void setValueForProperty(Object value)
    {
        _propertyEditModel.getConduit().set(_object, value);
    }

    /** Provide a value encoder for an enum type. */
    @SuppressWarnings("unchecked")
    public ValueEncoder getValueEncoderForProperty()
    {
        return new EnumValueEncoder(_propertyEditModel.getPropertyType());
    }

    /** Provide a select mode for an enum type. */
    @SuppressWarnings("unchecked")
    public SelectModel getSelectModelForProperty()
    {
        return new EnumSelectModel(_propertyEditModel.getPropertyType(), _resources
                .getContainerMessages());
    }

    public Form getForm()
    {
        return _form;
    }
}
