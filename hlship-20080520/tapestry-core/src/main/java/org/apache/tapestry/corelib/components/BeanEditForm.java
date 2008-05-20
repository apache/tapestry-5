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
import org.apache.tapestry.annotation.Component;
import org.apache.tapestry.annotation.Parameter;
import org.apache.tapestry.annotation.Property;
import org.apache.tapestry.annotation.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry.ioc.annotation.Inject;
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
    @Parameter(value = "message:submit-label", defaultPrefix = BindingConstants.LITERAL)
    @Property
    private String submitLabel;

    /**
     * The object to be edited. This will be read when the component renders and updated when the form for the component
     * is submitted. Typically, the container will listen for a "prepare" event, in order to ensure that a non-null
     * value is ready to be read or updated. Often, the BeanEditForm can create the object as needed (assuming a public,
     * no arguments constructor).  The object property defaults to a property with the same name as the component id.
     */
    @SuppressWarnings("unused")
    @Parameter(required = true)
    @Property
    private Object object;

    /**
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link org.apache.tapestry.beaneditor.BeanModel}.
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

    /**
     * If true, the default, then the embedded Form component will use client-side validation.
     */
    @Parameter
    private boolean clientValidation = true;

    /**
     * Binding the zone parameter will cause the form submission to be handled as an Ajax request that updates the
     * indicated zone.  Often a BeanEditForm will update the same zone that contains it.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    @Component(parameters = { "clientValidation=inherit:clientValidation", "zone=inherit:zone" })
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
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource beanModelSource;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return defaultProvider.defaultBinding("object", resources);
    }


    void onPrepareFromForm()
    {
        resources.triggerEvent(Form.PREPARE, null, null);

        if (model == null)
        {
            Class beanType = resources.getBoundType("object");

            model = beanModelSource.create(beanType, true, resources.getContainerResources());
        }

        BeanModelUtils.modify(model, null, include, exclude, reorder);
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
