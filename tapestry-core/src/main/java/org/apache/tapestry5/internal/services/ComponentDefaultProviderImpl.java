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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.bindings.InvariantBinding;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;

public class ComponentDefaultProviderImpl implements ComponentDefaultProvider
{
    private final PropertyAccess propertyAccess;

    private final BindingSource bindingSource;

    private final ValueEncoderSource valueEncoderSource;

    private final FieldTranslatorSource fieldTranslatorSource;

    private final FieldValidatorDefaultSource fieldValidatorDefaultSource;

    static final FieldValidator NOOP_VALIDATOR = new FieldValidator()
    {
        public void validate(Object value) throws ValidationException
        {
            // Do nothing
        }

        public void render(MarkupWriter writer)
        {
        }

        public boolean isRequired()
        {
            return false;
        }
    };

    public ComponentDefaultProviderImpl(PropertyAccess propertyAccess, BindingSource bindingSource,
                                        ValueEncoderSource valueEncoderSource,
                                        FieldTranslatorSource fieldTranslatorSource,
                                        FieldValidatorDefaultSource fieldValidatorDefaultSource)
    {
        this.propertyAccess = propertyAccess;
        this.bindingSource = bindingSource;
        this.valueEncoderSource = valueEncoderSource;
        this.fieldTranslatorSource = fieldTranslatorSource;
        this.fieldValidatorDefaultSource = fieldValidatorDefaultSource;
    }

    public String defaultLabel(ComponentResources resources)
    {
        Defense.notNull(resources, "resources");

        String componentId = resources.getId();
        String key = componentId + "-label";

        Messages containerMessages = resources.getContainerResources().getMessages();

        if (containerMessages.contains(key)) return containerMessages.get(key);

        return TapestryInternalUtils.toUserPresentable(componentId);
    }

    public Binding defaultBinding(String parameterName, ComponentResources resources)
    {
        Defense.notBlank(parameterName, "parameterName");
        Defense.notNull(resources, "resources");

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

    @SuppressWarnings({ "unchecked" })
    public ValueEncoder defaultValueEncoder(String parameterName, ComponentResources resources)
    {
        Defense.notBlank(parameterName, "parameterName");
        Defense.notNull(resources, "resources");

        Class parameterType = resources.getBoundType(parameterName);

        if (parameterType == null) return null;

        return valueEncoderSource.getValueEncoder(parameterType);
    }

    public FieldTranslator defaultTranslator(String parameterName, ComponentResources resources)
    {
        return fieldTranslatorSource.createDefaultTranslator(resources, parameterName);
    }

    public Binding defaultTranslatorBinding(final String parameterName, final ComponentResources resources)
    {
        String description = String.format("default translator, parameter %s of %s",
                                           parameterName, resources.getCompleteId());

        return new InvariantBinding(resources.getLocation(), FieldTranslator.class, description)
        {
            public Object get()
            {
                return defaultTranslator(parameterName, resources);
            }
        };
    }

    public FieldValidator defaultValidator(String parameterName, ComponentResources resources)
    {
        FieldValidator result = fieldValidatorDefaultSource.createDefaultValidator(resources, parameterName);

        return result == null ? NOOP_VALIDATOR : result;
    }

    public Binding defaultValidatorBinding(final String parameterName, final ComponentResources resources)
    {
        String description = String.format("default validator, parameter %s of %s", parameterName,
                                           resources.getCompleteId());

        return new InvariantBinding(resources.getLocation(), FieldValidator.class, description)
        {
            public Object get()
            {
                return defaultValidator(parameterName, resources);
            }
        };
    }
}
