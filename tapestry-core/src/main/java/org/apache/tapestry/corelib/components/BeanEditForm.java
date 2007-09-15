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

import org.apache.tapestry.Binding;
import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FormValidationControl;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * A component that creates an entire form editing the properties of a particular bean. Inspired by
 * <a href="http://www.trailsframework.org/">Trails</a> and <a
 * href="http://beanform.sourceforge.net/">BeanForm</a> (both for Tapestry 4). Generates a simple
 * UI for editing the properties of a JavaBean, with the flavor of UI for each property (text
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
 * @see PropertyEditor
 */
@SupportsInformalParameters
public class BeanEditForm implements ClientElement, FormValidationControl
{
    /** The text label for the submit button of the form, by default "Create/Update". */
    @Parameter(value = "message:submit-label", defaultPrefix = "literal")
    private String _submitLabel;

    /**
     * The object to be edited by the BeanEditor. This will be read when the component renders and
     * updated when the form for the component is submitted. Typically, the container will listen
     * for a "prepare" event, in order to ensure that a non-null value is ready to be read or
     * updated.
     */
    @Parameter(required = true)
    private Object _object;

    /** If true, the default, then the embedded Form component will use client-side validation. */
    @SuppressWarnings("unused")
    @Parameter
    private boolean _clientValidation;

    @Inject
    private ComponentResources _resources;

    @Inject
    private BeanModelSource _modelSource;

    @Component(parameters = "clientValidation=inherit:clientValidation")
    private Form _form;

    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect.
     * If not specified, a default bean model will be created from the type of the object bound to
     * the object parameter.
     */
    @Parameter
    private BeanModel _model;

    // Values that change with each change to the current property:

    private String _propertyName;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

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

    }

    boolean onPrepareFromForm()
    {
        // Fire a new prepare event to be consumed by the container. This is the container's
        // chance to ensure that there's an object to edit.

        _resources.triggerEvent(Form.PREPARE, null, null);

        // Now check to see if the value is null.

        // The only problem here is that if the bound property is backed by a persistent field, it
        // is assigned (and stored to the session, and propagated around the cluster) first,
        // before values are assigned.

        if (_object == null) _object = createDefaultObject();

        assert _object != null;

        if (_model == null)
        {
            Class<? extends Object> beanType = _object.getClass();

            _model = _modelSource.create(beanType, true, _resources.getContainerResources());
        }

        // Abort the form's prepare event, as we've already sent a prepare on its behalf.
        return true;
    }

    /** Used for testing. */
    void inject(ComponentResources resources, BeanModelSource modelSource)
    {
        _resources = resources;
        _modelSource = modelSource;
    }

    /** Returns the object being edited. */
    public Object getObject()
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

    /** Returns the client id of the embedded form. */
    public String getClientId()
    {
        return _form.getClientId();
    }

    public String getSubmitLabel()
    {
        return _submitLabel;
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
