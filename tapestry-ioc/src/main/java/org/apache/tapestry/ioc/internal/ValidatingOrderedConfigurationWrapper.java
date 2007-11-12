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

package org.apache.tapestry.ioc.internal;

import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.slf4j.Logger;

/**
 * Implements validation of values provided to an
 * {@link org.apache.tapestry.ioc.OrderedConfiguration}. If you provide an incorrect value type,
 * the value is converted to null but added anyway. This ensures that incorrect values contributed
 * in don't screw up the {@link org.apache.tapestry.ioc.internal.util.Orderer} (and generate a bunch
 * of error messages there).
 *
 * @param <T>
 */
public class ValidatingOrderedConfigurationWrapper<T> implements OrderedConfiguration<T>
{
    private final String _serviceId;

    private final ContributionDef _contributionDef;

    private final Logger _logger;

    private final Class _expectedType;

    private final OrderedConfiguration<T> _delegate;

    public ValidatingOrderedConfigurationWrapper(String serviceId, ContributionDef contributionDef,
                                                 Logger logger, Class expectedType, OrderedConfiguration<T> delegate)
    {
        _serviceId = serviceId;
        _contributionDef = contributionDef;
        _logger = logger;
        _expectedType = expectedType;
        _delegate = delegate;
    }

    public void add(String id, T object, String... constraints)
    {
        _delegate.add(id, validVersionOf(object), constraints);
    }

    private T validVersionOf(T object)
    {
        if (object == null || _expectedType.isInstance(object)) return object;

        _logger.warn(IOCMessages.contributionWrongValueType(_serviceId, _contributionDef, object
                .getClass(), _expectedType));

        return null;
    }
}
