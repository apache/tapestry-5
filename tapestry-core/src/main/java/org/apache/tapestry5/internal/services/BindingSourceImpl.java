// Copyright 2006-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.internal.services.StringInterner;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.BindingSource;

import java.util.Map;

public class BindingSourceImpl implements BindingSource
{
    private final Map<String, BindingFactory> factories;

    private final StringInterner interner;

    public BindingSourceImpl(Map<String, BindingFactory> factories, StringInterner interner)
    {
        this.factories = factories;
        this.interner = interner;
    }

    public Binding newBinding(String description, ComponentResources container, String defaultPrefix, String expression)
    {
        return newBinding(description, container, container, defaultPrefix, expression, null);
    }

    public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                              String defaultPrefix, String expression, Location location)
    {
        assert InternalUtils.isNonBlank(description);
        assert container != null;
        assert InternalUtils.isNonBlank(defaultPrefix);
        assert component != null;

        // TAP5-845: The expression may be the empty string. This is ok, if it's compatible with
        // the default prefix (the empty string is not a valid property expression, but is valid
        // as a literal string, perhaps as an informal parameter).

        // Location might be null

        String subexpression = expression;
        int colonx = expression.indexOf(':');

        BindingFactory factory = null;

        if (colonx > 0)
        {
            String prefix = expression.substring(0, colonx);

            factory = factories.get(prefix);
            if (factory != null)
                subexpression = expression.substring(colonx + 1);
        }

        if (factory == null)
            factory = factories.get(defaultPrefix);

        // And if that's null, what then? We assume that the default prefix is a valid prefix,
        // or we'll get an NPE below and report it like any other error.

        try
        {
            return factory.newBinding(interner.intern(description), container, component, subexpression, location);
        } catch (Exception ex)
        {
            throw new TapestryException(String.format("Could not convert '%s' into a component parameter binding: %s", expression, ExceptionUtils.toMessage(ex)), location, ex);
        }
    }
}
