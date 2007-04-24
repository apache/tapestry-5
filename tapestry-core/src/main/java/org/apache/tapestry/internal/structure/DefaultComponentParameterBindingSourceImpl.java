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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.DefaultComponentParameterBindingSource;

public class DefaultComponentParameterBindingSourceImpl implements
        DefaultComponentParameterBindingSource
{
    private final PropertyAccess _propertyAccess;

    private final BindingSource _bindingSource;

    public DefaultComponentParameterBindingSourceImpl(PropertyAccess propertyAccess,
            BindingSource bindingSource)
    {
        _propertyAccess = propertyAccess;
        _bindingSource = bindingSource;
    }

    public Binding createDefaultBinding(String parameterName, ComponentResources componentResources)
    {
        String componentId = componentResources.getId();

        Component container = componentResources.getContainer();

        // Only provide a default binding if the container actually contains the property.
        // This sets up an error condition for when the parameter is not bound, and
        // the binding can't be deduced.

        if (_propertyAccess.getAdapter(container).getPropertyAdapter(componentId) == null)
            return null;

        ComponentResources containerResources = componentResources.getContainerResources();

        return _bindingSource.newBinding(
                "default " + parameterName,
                containerResources,
                TapestryConstants.PROP_BINDING_PREFIX,
                componentId);
    }

}
