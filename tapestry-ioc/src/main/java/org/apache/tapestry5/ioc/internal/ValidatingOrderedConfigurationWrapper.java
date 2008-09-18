// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * Implements validation of values provided to an {@link org.apache.tapestry5.ioc.OrderedConfiguration}. If you provide
 * an incorrect value type, the value is converted to null but added anyway. This ensures that incorrect values
 * contributed in don't screw up the {@link org.apache.tapestry5.ioc.internal.util.Orderer} (and generate a bunch of
 * error messages there).
 *
 * @param <T>
 */
public class ValidatingOrderedConfigurationWrapper<T> implements OrderedConfiguration<T>
{
    private final String serviceId;

    private final Class expectedType;

    private final OrderedConfiguration<T> delegate;

    public ValidatingOrderedConfigurationWrapper(String serviceId, Class expectedType, OrderedConfiguration<T> delegate)
    {
        this.serviceId = serviceId;
        this.expectedType = expectedType;
        this.delegate = delegate;
    }

    public void add(String id, T object, String... constraints)
    {
        checkValid(object);

        delegate.add(id, object, constraints);
    }

    public void addInstance(String id, Class<? extends T> clazz, String... constraints)
    {
        Defense.notNull(clazz, "clazz");

        if (!expectedType.isAssignableFrom(clazz))
            throw new IllegalArgumentException(IOCMessages.wrongContributionClass(clazz, serviceId, expectedType));

        delegate.addInstance(id, clazz, constraints);
    }

    private void checkValid(T object)
    {
        if (object == null || expectedType.isInstance(object)) return;

        throw new IllegalArgumentException(IOCMessages.contributionWrongValueType(serviceId, object
                .getClass(), expectedType));
    }
}
