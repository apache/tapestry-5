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

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Property;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * Used to display the properties of a bean, using an underlying {@link BeanModel}. The output is a series of
 * &lt;div&gt; elements for the property names and property values.   Only properties that have a known data type are
 * displayed.
 *
 * @see org.apache.tapestry.beaneditor.DataType
 * @see BeanModel
 */
@SupportsInformalParameters
public class BeanDisplay
{

    /**
     * The object to be rendered; if not explicitly bound, a default binding to a property whose name matches this
     * component's id will be used.
     */
    @Parameter(required = true)
    @Property(write = false)
    private Object object;

    /**
     * If true, then &lt;span&gt; tags around each output property will be omitted. If false, then a span tag (to
     * identify the id of each property as the CSS class attribute) will be included.
     */
    @Parameter(value = "false")
    private boolean lean;

    /**
     * The model that identifies the parameters to be displayed, their order, and every other aspect. If not specified,
     * a default bean model will be created from the type of the object bound to the object parameter.
     */
    @Parameter
    @Property(write = false)
    private BeanModel model;

    /**
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link BeanModel}. The names are
     * case-insensitive.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String reorder;

    /**
     * Where to search for local overrides of property display blocks as block parameters. Further, the container of the
     * overrides is used as the source for overridden validation messages. This is normally the component itself, but
     * when the component is used within a BeanEditForm, it will be the BeanEditForm's block parameter that will be
     * searched.
     */
    @Parameter(value = "componentResources")
    @Property(write = false)
    private ComponentResources overrides;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource modelSource;

    @Property
    private String propertyName;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return defaultProvider.defaultBinding("object", resources);
    }

    void setupRender()
    {
        if (model == null) model = modelSource.create(object.getClass(), false, overrides
                .getContainerResources());

        BeanModelUtils.modify(model, null, include, exclude, reorder);
    }

    /**
     * Returns the property model for the current property.
     */
    public PropertyModel getPropertyModel()
    {
        return model.get(propertyName);
    }

    public String getLabelClass()
    {
        return generateClassValue("t-beandisplay-label");
    }

    private String generateClassValue(String className)
    {
        if (lean) return className;

        return className + " " + getPropertyModel().getId();
    }

    public String getValueClass()
    {
        return generateClassValue("t-beandisplay-value");
    }

}
