// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Map;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.BindingSource;

public class BindingSourceImpl implements BindingSource
{
    private final Map<String, BindingFactory> _factories;

    public BindingSourceImpl(Map<String, BindingFactory> factories)
    {
        _factories = factories;
    }

    public Binding newBinding(String description, ComponentResources container,
            String defaultPrefix, String expression)
    {
        return newBinding(description, container, container, defaultPrefix, expression, null);
    }

    public Binding newBinding(String description, ComponentResources container,
            ComponentResources component, String defaultPrefix, String expression, Location location)
    {
        notBlank(description, "description");
        notNull(container, "container");
        notNull(component, "component");
        notBlank(defaultPrefix, "defaultPrefix");
        notBlank(expression, "expression");
        // Location might be null

        String subexpression = expression;
        int colonx = expression.indexOf(':');

        BindingFactory factory = null;

        if (colonx > 0)
        {
            String prefix = expression.substring(0, colonx);

            factory = _factories.get(prefix);
            if (factory != null)
                subexpression = expression.substring(colonx + 1);
        }

        if (factory == null)
            factory = _factories.get(defaultPrefix);

        // And if that's null, what then? We assume that the default prefix is a valid prefix,
        // or we'll get an NPE below and report it like any other error.

        try
        {
            return factory.newBinding(description, container, component, subexpression, location);
        }
        catch (Exception ex)
        {
            throw new TapestryException(ServicesMessages.bindingSourceFailure(expression, ex),
                    location, ex);
        }
    }
}
