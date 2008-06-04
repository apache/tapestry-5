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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.TranslatorSource;
import org.apache.tapestry5.services.ValueEncoderSource;

public class ComponentDefaultProviderImpl implements ComponentDefaultProvider
{
    private final PropertyAccess propertyAccess;

    private final BindingSource bindingSource;

    private final ValueEncoderSource valueEncoderSource;

    private final TranslatorSource translatorSource;

    public ComponentDefaultProviderImpl(PropertyAccess propertyAccess, BindingSource bindingSource,
                                        ValueEncoderSource valueEncoderSource, TranslatorSource translatorSource)
    {
        this.propertyAccess = propertyAccess;
        this.bindingSource = bindingSource;
        this.valueEncoderSource = valueEncoderSource;
        this.translatorSource = translatorSource;
    }

    public String defaultLabel(ComponentResources resources)
    {
        notNull(resources, "resources");

        String componentId = resources.getId();
        String key = componentId + "-label";

        Messages containerMessages = resources.getContainerResources().getMessages();

        if (containerMessages.contains(key)) return containerMessages.get(key);

        return TapestryInternalUtils.toUserPresentable(componentId);
    }

    public Binding defaultBinding(String parameterName, ComponentResources resources)
    {
        notBlank(parameterName, "parameterName");
        notNull(resources, "resources");

        String componentId = resources.getId();

        Component container = resources.getContainer();

        // Only provide a default binding if the container actually contains the property.
        // This sets up an error condition for when the parameter is not bound, and
        // the binding can't be deduced.

        if (propertyAccess.getAdapter(container).getPropertyAdapter(componentId) == null)
            return null;

        ComponentResources containerResources = resources.getContainerResources();

        return bindingSource.newBinding(
                "default " + parameterName,
                containerResources,
                BindingConstants.PROP,
                componentId);
    }

    public ValueEncoder defaultValueEncoder(String parameterName, ComponentResources resources)
    {
        notBlank(parameterName, "parameterName");
        notNull(resources, "resources");

        Class parameterType = resources.getBoundType(parameterName);

        if (parameterType == null) return null;

        return valueEncoderSource.getValueEncoder(parameterType);
    }

    public Translator defaultTranslator(String parameterName, ComponentResources resources)
    {
        notBlank(parameterName, "parameterName");
        notNull(resources, "resources");

        Class type = resources.getBoundType(parameterName);

        if (type == null) return null;

        return translatorSource.findByType(type);
    }
}
