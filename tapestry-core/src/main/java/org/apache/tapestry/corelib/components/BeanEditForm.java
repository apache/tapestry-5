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
import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.FormValidationControl;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.TranslatorDefaultSource;
import org.apache.tapestry.util.EnumSelectModel;
import org.apache.tapestry.util.EnumValueEncoder;

/**
 * A component that creates an entire form editting the properties of a particular bean. Inspired by
 * <a href="http://www.trailsframework.org/">Trails</a> and <a
 * href="http://beanform.sourceforge.net/">BeanForm</a> (both for Tapestry 4). Generates a simple
 * UI for editting the properties of a JavaBean, with the flavor of UI for each property (text
 * field, checkbox, drop down list) determined from the property type, and the order and validation
 * for the properties determined from annotations on the property's getter and setter methods.
 * <p>
 * You may add &lt;t:parameter&gt;s to the component; when the name matches (case insensitive) the
 * name of a property, then the corresponding Block is renderered, rather than any of the built in
 * property editor blocks. This allows you to override specific properties with your own customized
 * UI, for cases where the default UI is insufficient, or no built-in editor type is appropriate.
 * <p>
 * This component is likely to change more than any other thing in Tapestry! What's available now is
 * a very limited preview of its eventual functionality.
 * 
 * @see BeanModel
 * @see BeanModelSource
 */
@SupportsInformalParameters
public class BeanEditForm implements ClientElement, FormValidationControl
{
    /** The text label for the submit button of the form, by default "Create/Update". */
    @Parameter(value = "message:submit-label", defaultPrefix = "literal")
    private String _submitLabel;

    /**
     * The object to be editted by the BeanEditor. This will be read when the component renders and
     * updated when the form for the component is submitted. Typically, the container will listen
     * for a "prepare" event, in order to ensure that a non-null value is ready to be read or
     * updated.
     */
    @Parameter(required = true)
    private Object _object;

    /** If true, the default, then the embedded Form component will use client-side validation. */
    @Parameter("true")
    private boolean _clientValidation;

    @Inject
    private ComponentResources _resources;

    @Inject
    private BeanModelSource _modelSource;

    @Inject
    private TranslatorDefaultSource _translatorDefaultSource;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private Block _text;

    @Inject
    private Block _enum;

    @Inject
    private Block _checkbox;

    @Component(parameters = "clientValidation=clientValidation")
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

    /**
     * The model that identifies the parameters to be editted, their order, and every other aspect.
     * If not specified, a default bean model will be created from the type of the object bound to
     * the object parameter.
     */
    @Parameter
    private BeanModel _model;

    // Values that change with each change to the current property:

    private String _propertyName;

    private PropertyModel _propertyEditModel;

    private Block _blockForProperty;

    private Field _fieldForProperty;

    public BeanModel getModel()
    {
        return _model;
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyName = propertyName;

        _propertyEditModel = _model.get(propertyName);

        _blockForProperty = null;
        _fieldForProperty = null;

        Block override = _resources.getBlockParameter(_propertyEditModel.getId());

        if (override != null)
        {
            _blockForProperty = override;
            return;
        }

        String dataType = _propertyEditModel.getDataType();

        if (dataType.equals("text"))
        {
            _blockForProperty = _text;
            _fieldForProperty = _textField;
            return;
        }

        if (dataType.equals("enum"))
        {
            _blockForProperty = _enum;
            _fieldForProperty = _select;
            return;
        }

        if (dataType.equals("checkbox"))
        {
            _blockForProperty = _checkbox;
            _fieldForProperty = _checkboxField;
            return;
        }

        throw new IllegalArgumentException(_messages.format("no-editor", dataType, propertyName));
    }

    boolean onPrepareFromForm()
    {
        // Fire a new prepare event to be consumed by the container. This is the container's
        // chance to ensure that there's an object to edit.

        _resources.triggerEvent(Form.PREPARE, null, null);

        // Now check to see if the value is null.

        // The only problem here is that if the bound property is backed by a persistent field, it
        // is assigned (and stored to the session, and propogated around the cluster) first,
        // before values are assigned.

        if (_object == null) _object = createDefaultObject();

        assert _object != null;

        if (_model == null)
        {
            Class<? extends Object> beanType = _object.getClass();

            _model = _modelSource.create(beanType, true, _resources.getContainerResources());
        }

        return true; // abort the form's prepare event
    }

    void inject(ComponentResources resources, BeanModelSource modelSource)
    {
        _resources = resources;
        _modelSource = modelSource;
    }

    Object getObject()
    {
        return _object;
    }

    private Object createDefaultObject()
    {
        Class type = _resources.getBoundType("object");

        try
        {
            return type.newInstance();
        }
        catch (Exception ex)
        {
            throw new TapestryException(ComponentMessages.failureInstantiatingObject(
                    type,
                    _resources.getCompleteId(),
                    ex), _resources.getLocation(), ex);
        }
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

    public PropertyModel getPropertyEditModel()
    {
        return _propertyEditModel;
    }

    public Block getBlockForProperty()
    {
        return _blockForProperty;
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

    /** Returns the client id of the embedded form. */
    public String getClientId()
    {
        return _form.getClientId();
    }

    public String getSubmitLabel()
    {
        return _submitLabel;
    }

    public boolean getClientValidation()
    {
        return _clientValidation;
    }

    public void clearErrors()
    {
        _form.clearErrors();
    }

    public boolean getHasErrors()
    {
        return _form.getHasErrors();
    }

    public boolean isValid()
    {
        return _form.isValid();
    }

    public void recordError(Field field, String errorMessage)
    {
        _form.recordError(field, errorMessage);
    }

    public void recordError(String errorMessage)
    {
        _form.recordError(errorMessage);
    }

}
