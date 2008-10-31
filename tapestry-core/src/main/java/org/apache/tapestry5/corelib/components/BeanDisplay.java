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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

/**
 * Used to display the properties of a bean, using an underlying {@link BeanModel}. The output definition list: a
 * &lt;dl&gt; element containing a series of &lt;dt&gt;/&lt;dd&gt; pairs.  The property label is used as the &lt;dt&gt;
 * and the property value (formatted as per the datatype) is the &lt;dd&gt;. Only properties that have a known data type
 * are displayed.
 * <p/>
 * The property id is used as the class attribute of the &lt;dt&gt; and &lt;dd&gt; element, allowing CSS customization
 * per property.  This does not occur when lean is bound to true.
 * <p/>
 * The outer &lt;dl&gt; element has the CSS class "t-beandisplay".
 *
 * @see org.apache.tapestry5.beaneditor.DataType
 * @see BeanModel
 */
@SupportsInformalParameters
public class BeanDisplay
{

    /**
     * The object to be rendered; if not explicitly bound, a default binding to a property whose name matches this
     * component's id will be used.
     */
    @Parameter(required = true, allowNull = false, autoconnect = true)
    @Property(write = false)
    private Object object;

    /**
     * If true, then the CSS class attribute on the &lt;dt&gt; and &lt;dd&gt; elements will be ommitted.
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
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link BeanModel}. The names are
     * case-insensitive.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
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

    /**
     * A comma-separated list of property names to be added to the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String add;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource modelSource;

    @Property
    private String propertyName;

    void setupRender()
    {
        if (model == null) model = modelSource.createDisplayModel(object.getClass(), overrides.getContainerMessages());

        BeanModelUtils.modify(model, add, include, exclude, reorder);
    }

    /**
     * Returns the property model for the current property.
     */
    public PropertyModel getPropertyModel()
    {
        return model.get(propertyName);
    }


    public String getPropertyClass()
    {
        return lean ? null : getPropertyModel().getId();
    }
}
