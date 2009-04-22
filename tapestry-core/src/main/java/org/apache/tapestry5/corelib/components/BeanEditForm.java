// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

/**
 * A component that creates an entire form editing the properties of a particular bean. Inspired by <a
 * href="http://www.trailsframework.org/">Trails</a> and <a href="http://beanform.sourceforge.net/">BeanForm</a> (both
 * for Tapestry 4). Generates a simple UI for editing the properties of a JavaBean, with the flavor of UI for each
 * property (text field, checkbox, drop down list) determined from the property type (or by other means, such as an
 * annotation), and the order and validation for the properties determined from annotations on the property's getter and
 * setter methods.
 * <p/>
 * You may add block parameters to the component; when the name matches (case insensitive) the name of a property, then
 * the corresponding Block is renderered, rather than any of the built in property editor blocks. This allows you to
 * override specific properties with your own customized UI, for cases where the default UI is insufficient, or no
 * built-in editor type is appropriate.
 * <p/>
 * BeanEditForm contains a {@link org.apache.tapestry5.corelib.components.Form} component and will trigger all the
 * events of a Form.
 *
 * @see org.apache.tapestry5.beaneditor.BeanModel
 * @see org.apache.tapestry5.services.BeanModelSource
 * @see org.apache.tapestry5.corelib.components.PropertyEditor
 * @see org.apache.tapestry5.beaneditor.DataType
 */
@SupportsInformalParameters
@Events(EventConstants.PREPARE)
public class BeanEditForm implements ClientElement, FormValidationControl
{

    /**
     * The text label for the submit button of the form, by default "Create/Update".
     */
    @Parameter(value = "message:submit-label", defaultPrefix = BindingConstants.LITERAL)
    @Property
    private String submitLabel;

    /**
     * The object to be edited. This will be read when the component renders and updated when the form for the component
     * is submitted. Typically, the container will listen for a "prepare" event, in order to ensure that a non-null
     * value is ready to be read or updated. Often, the BeanEditForm can create the object as needed (assuming a public,
     * no arguments constructor).  The object property defaults to a property with the same name as the component id.
     */
    @Parameter(required = true, autoconnect = true)
    @Property
    private Object object;

    /**
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String include;

    /**
     * A comma-separated list of property names to be added to the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String add;

    /**
     * A comma-separated list of property names to be removed from the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     * The names are case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String reorder;

    @Component(parameters =
            "validationId=componentResources.id", publishParameters = "clientValidation,autofocus,zone")
    private Form form;

    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect. If not specified, a
     * default bean model will be created from the type of the object bound to the object parameter.
     */
    @SuppressWarnings("unused")
    @Parameter
    @Property
    private BeanModel model;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource beanModelSource;


    void onPrepareFromForm()
    {
        resources.triggerEvent(EventConstants.PREPARE, null, null);

        if (model == null)
        {
            Class beanType = resources.getBoundType("object");

            model = beanModelSource.createEditModel(beanType, resources.getContainerMessages());
        }

        BeanModelUtils.modify(model, add, include, exclude, reorder);
    }


    /**
     * Returns the client id of the embedded form.
     */
    public String getClientId()
    {
        return form.getClientId();
    }

    public void clearErrors()
    {
        form.clearErrors();
    }

    public boolean getHasErrors()
    {
        return form.getHasErrors();
    }

    public boolean isValid()
    {
        return form.isValid();
    }

    public void recordError(Field field, String errorMessage)
    {
        form.recordError(field, errorMessage);
    }

    public void recordError(String errorMessage)
    {
        form.recordError(errorMessage);
    }
}
