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

import org.apache.tapestry.Binding;
import org.apache.tapestry.Block;
import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.FormValidationControl;
import org.apache.tapestry.Translator;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BeanBlockSource;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.PropertyEditContext;
import org.apache.tapestry.services.TranslatorDefaultSource;

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

    @Component(parameters = "clientValidation=clientValidation")
    private Form _form;

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

    @Inject
    private Environment _environment;

    @Inject
    private BeanBlockSource _beanBlockSource;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    private boolean _mustPopBeanEditContext;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return _defaultProvider.defaultBinding("object", _resources);
    }

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

        Block override = _resources.getBlockParameter(_propertyEditModel.getId());

        if (override != null)
        {
            _blockForProperty = override;
            return;
        }

        String dataType = _propertyEditModel.getDataType();

        try
        {
            _blockForProperty = _beanBlockSource.getEditBlock(dataType);
        }
        catch (RuntimeException ex)
        {
            String message = _messages.format("block-error", _propertyName, dataType, _object, ex);

            throw new TapestryException(message, _resources.getLocation(), ex);
        }
    }

    boolean onPrepareFromForm()
    {
        PropertyEditContext context = new PropertyEditContext()
        {
            public Messages getContainerMessages()
            {
                return _resources.getContainerMessages();
            }

            public String getLabel()
            {
                return _propertyEditModel.getLabel();
            }

            public String getPropertyId()
            {
                return _propertyEditModel.getId();
            }

            public Class getPropertyType()
            {
                return _propertyEditModel.getPropertyType();
            }

            public Object getPropertyValue()
            {
                return _propertyEditModel.getConduit().get(getObject());
            }

            public Translator getTranslator()
            {
                return _translatorDefaultSource.find(_propertyEditModel.getPropertyType());
            }

            public FieldValidator getValidator(Field field)
            {
                return _fieldValidatorDefaultSource.createDefaultValidator(
                        field,
                        _propertyName,
                        _resources.getContainerMessages(),
                        _locale,
                        _propertyEditModel.getPropertyType(),
                        _propertyEditModel.getConduit());
            }

            public void setPropertyValue(Object value)
            {
                _propertyEditModel.getConduit().set(getObject(), value);
            }
        };

        _environment.push(PropertyEditContext.class, context);
        // Depending on whether we're rendering or processing the form submission we'll have two
        // different places to clean up the Environment.
        _mustPopBeanEditContext = true;

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

    private void cleanupBeanEditContext()
    {
        if (_mustPopBeanEditContext)
        {
            _environment.pop(PropertyEditContext.class);
            _mustPopBeanEditContext = false;
        }
    }

    void onSubmit()
    {
        cleanupBeanEditContext();
    }

    void afterRender()
    {
        cleanupBeanEditContext();
    }

    /** Used for testing. */
    void inject(ComponentResources resources, BeanModelSource modelSource, Environment environment)
    {
        _resources = resources;
        _modelSource = modelSource;
        _environment = environment;
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

    public Block getBlockForProperty()
    {
        return _blockForProperty;
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
