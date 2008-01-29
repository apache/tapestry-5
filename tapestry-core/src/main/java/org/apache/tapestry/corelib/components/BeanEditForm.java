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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.corelib.internal.InternalMessages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * A component that creates an entire form editing the properties of a particular bean. Inspired by <a
 * href="http://www.trailsframework.org/">Trails</a> and <a href="http://beanform.sourceforge.net/">BeanForm</a> (both
 * for Tapestry 4). Generates a simple UI for editing the properties of a JavaBean, with the flavor of UI for each
 * property (text field, checkbox, drop down list) determined from the property type, and the order and validation for
 * the properties determined from annotations on the property's getter and setter methods.
 * <p/>
 * You may add &lt;t:parameter&gt;s to the component; when the name matches (case insensitive) the name of a property,
 * then the corresponding Block is renderered, rather than any of the built in property editor blocks. This allows you
 * to override specific properties with your own customized UI, for cases where the default UI is insufficient, or no
 * built-in editor type is appropriate.
 *
 * @see BeanModel
 * @see BeanModelSource
 * @see PropertyEditor
 */
@SupportsInformalParameters
public class BeanEditForm implements ClientElement, FormValidationControl
{
    /**
     * The text label for the submit button of the form, by default "Create/Update".
     */
    @Parameter(value = "message:submit-label", defaultPrefix = "literal")
    private String _submitLabel;

    /**
     * The object to be edited by the BeanEditor. This will be read when the component renders and updated when the form
     * for the component is submitted. Typically, the container will listen for a "prepare" event, in order to ensure
     * that a non-null value is ready to be read or updated. Often, the BeanEditForm can create the object as needed
     * (assuming a public, no arguments constructor).  The object property defaults to a property with the same name as
     * the component id.
     */
    @SuppressWarnings("unused")
    @Parameter(required = true)
    private Object _object;

    /**
     * A comma-separated list of property names to be removed from the {@link BeanModel}. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = "literal")
    private String _remove;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = "literal")
    private String _reorder;

    /**
     * If true, the default, then the embedded Form component will use client-side validation.
     */
    @SuppressWarnings("unused")
    @Parameter
    private boolean _clientValidation = true;

    @Component(parameters = "clientValidation=inherit:clientValidation")
    private Form _form;

    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect. If not specified, a
     * default bean model will be created from the type of the object bound to the object parameter.
     */
    @SuppressWarnings("unused")
    @Parameter
    private BeanModel _model;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private ComponentResources _resources;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return _defaultProvider.defaultBinding("object", _resources);
    }

    void onPrepareFromForm()
    {
        // Fire a new prepare event to be consumed by the container. This is the container's
        // chance to ensure that there's an object to edit.

        _resources.triggerEvent(Form.PREPARE, null, null);

        if (_object == null) _object = createDefaultObject();

        assert _object != null;
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
            throw new TapestryException(
                    InternalMessages.failureInstantiatingObject(type, _resources.getCompleteId(), ex),
                    _resources.getLocation(), ex);
        }
    }

    public Object getObject()
    {
        return _object;
    }

    /**
     * Returns the client id of the embedded form.
     */
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

    void inject(ComponentResources resources)
    {
        _resources = resources;
    }

}
