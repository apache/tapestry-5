// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import java.util.Map;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.Orderer;

/**
 * Wraps a {@link java.util.List} as a {@link org.apache.tapestry5.ioc.OrderedConfiguration}, implementing validation of
 * values provided to an {@link org.apache.tapestry5.ioc.OrderedConfiguration}.
 * 
 * @param <T>
 */
public class ValidatingOrderedConfigurationWrapper<T> extends AbstractConfigurationImpl<T> implements
        OrderedConfiguration<T>
{
    private final TypeCoercerProxy typeCoercer;

    private final Orderer<T> orderer;

    private final String serviceId;

    private final Class<T> expectedType;

    private final Map<String, OrderedConfigurationOverride<T>> overrides;

    private final ContributionDef contribDef;

    public ValidatingOrderedConfigurationWrapper(Class<T> expectedType, ObjectLocator locator,
            TypeCoercerProxy typeCoercer, Orderer<T> orderer, Map<String, OrderedConfigurationOverride<T>> overrides,
            ContributionDef contribDef, String serviceId)
    {
        super(expectedType, locator);
        this.typeCoercer = typeCoercer;

        this.orderer = orderer;
        this.overrides = overrides;
        this.contribDef = contribDef;
        this.serviceId = serviceId;
        this.expectedType = expectedType;
    }

    public void add(String id, T object, String... constraints)
    {
        T coerced = object == null ? null : typeCoercer.coerce(object, expectedType);

        orderer.add(id, coerced, constraints);
    }

    public void override(String id, T object, String... constraints)
    {
        assert InternalUtils.isNonBlank(id);

        T coerced = object == null ? null : typeCoercer.coerce(object, expectedType);

        OrderedConfigurationOverride<T> existing = overrides.get(id);

        if (existing != null)
            throw new IllegalArgumentException(String.format("Contribution '%s' has already been overridden (by %s).",
                    id, existing.getContribDef()));

        overrides.put(id, new OrderedConfigurationOverride<T>(orderer, id, coerced, constraints, contribDef));
    }

    public void addInstance(String id, Class<? extends T> clazz, String... constraints)
    {
        add(id, instantiate(clazz), constraints);
    }

    public void overrideInstance(String id, Class<? extends T> clazz, String... constraints)
    {
        override(id, instantiate(clazz), constraints);
    }
}
