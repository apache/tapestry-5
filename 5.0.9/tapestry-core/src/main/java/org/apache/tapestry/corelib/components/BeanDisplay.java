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
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * Used to display the properties of a bean, using an underlying {@link BeanModel}. The output is a
 * series of &lt;div&gt; elements for the property names and property values.
 */
@SupportsInformalParameters
public class BeanDisplay
{

    /**
     * The object to be rendered; if not explicitly bound, a default binding to a property whose
     * name matches this component's id will be used.
     */
    @Parameter(required = true)
    private Object _object;

    /**
     * The model that identifies the parameters to be displayed, their order, and every other
     * aspect. If not specified, a default bean model will be created from the type of the object
     * bound to the object parameter.
     */
    @Parameter
    private BeanModel _model;

    /**
     * A comma-separated list of property names to be removed from the {@link BeanModel}. The names
     * are case-insensitive.
     */
    @Parameter(defaultPrefix = "literal")
    private String _remove;

    /**
     * A comma-separated list of property names indicating the order in which the properties should
     * be presented. The names are case insensitive. Any properties not indicated in the list will
     * be appended to the end of the display order.
     */
    @Parameter(defaultPrefix = "literal")
    private String _reorder;

    /**
     * Where to search for local overrides of property display blocks as block parameters. Further,
     * the container of the overrides is used as the source for overridden validation messages. This
     * is normally the component itself, but when the component is used within a BeanEditForm, it
     * will be the BeanEditForm's block parameter that will be searched.
     */
    @Parameter(value = "componentResources")
    private ComponentResources _overrides;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private ComponentResources _resources;

    @Inject
    private BeanModelSource _modelSource;

    private String _propertyName;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return _defaultProvider.defaultBinding("object", _resources);
    }

    public Object getObject()
    {
        return _object;
    }

    void setupRender()
    {
        if (_model == null)
            _model = _modelSource.create(_object.getClass(), false, _overrides
                    .getContainerResources());

        if (_remove != null) BeanModelUtils.remove(_model, _remove);

        if (_reorder != null) BeanModelUtils.reorder(_model, _reorder);
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

    /**
     * Returns the property model for the current property.
     */
    public PropertyModel getPropertyModel()
    {
        return _model.get(_propertyName);
    }

    public ComponentResources getOverrides()
    {
        return _overrides;
    }

}
