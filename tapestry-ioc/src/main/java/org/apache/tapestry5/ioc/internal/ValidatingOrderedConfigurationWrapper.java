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

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.Orderer;

import java.util.Map;

/**
 * Wraps a {@link java.util.List} as a {@link org.apache.tapestry5.commons.OrderedConfiguration}, implementing validation of
 * values provided to an {@link org.apache.tapestry5.commons.OrderedConfiguration}.
 *
 * @param <T>
 */
public class ValidatingOrderedConfigurationWrapper<T> extends AbstractConfigurationImpl<T> implements
        OrderedConfiguration<T>
{
    private final TypeCoercerProxy typeCoercer;

    private final Orderer<T> orderer;

    private final Class<T> expectedType;

    private final Map<String, OrderedConfigurationOverride<T>> overrides;

    private final ContributionDef contribDef;

    // Used to supply a default ordering constraint when none is supplied.
    private String priorId;

    public ValidatingOrderedConfigurationWrapper(Class<T> expectedType, ObjectLocator locator,
                                                 TypeCoercerProxy typeCoercer, Orderer<T> orderer, Map<String, OrderedConfigurationOverride<T>> overrides,
                                                 ContributionDef contribDef)
    {
        super(expectedType, locator);
        this.typeCoercer = typeCoercer;

        this.orderer = orderer;
        this.overrides = overrides;
        this.contribDef = contribDef;
        this.expectedType = expectedType;
    }

    @Override
    public void add(String id, T object, String... constraints)
    {
        T coerced = object == null ? null : typeCoercer.coerce(object, expectedType);

        // https://issues.apache.org/jira/browse/TAP5-1565
        // Order each added contribution after the previously added contribution
        // (in the same method) if no other constraint is supplied.
        if (constraints.length == 0 && priorId != null)
        {
            // Ugly: reassigning parameters is yuck.
            constraints = new String[]{"after:" + priorId};
        }

        orderer.add(id, coerced, constraints);

        priorId = id;
    }

    @Override
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

    @Override
    public void addInstance(String id, Class<? extends T> clazz, String... constraints)
    {
        add(id, instantiate(clazz), constraints);
    }

    @Override
    public void overrideInstance(String id, Class<? extends T> clazz, String... constraints)
    {
        override(id, instantiate(clazz), constraints);
    }
}
