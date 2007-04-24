// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.IOCUtilities.toQualifiedId;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.def.ContributionDef;

/**
 * Implements validation of values provided to an
 * {@link org.apache.tapestry.ioc.OrderedConfiguration}. It also takes care of qualifying any ids.
 * If you provide an incorrect value type, the value is converted to null but added anyway. This
 * ensures that incorrect values contributed in don't screw up the
 * {@link org.apache.tapestry.ioc.internal.util.Orderer} (and generate a bunch of error messages there).
 * 
 * @param <T>
 */
public class ValidatingOrderedConfigurationWrapper<T> implements OrderedConfiguration<T>
{
    private final String _serviceId;

    /** Module id containing the contribution (not necessarily the service). */
    private final String _moduleId;

    private final ContributionDef _contributionDef;

    private final Log _log;

    private final Class _expectedType;

    private final OrderedConfiguration<T> _delegate;

    public ValidatingOrderedConfigurationWrapper(String serviceId, String moduleId,
            ContributionDef contributionDef, Log log, Class expectedType,
            OrderedConfiguration<T> delegate)
    {
        _serviceId = serviceId;
        _moduleId = moduleId;
        _contributionDef = contributionDef;
        _log = log;
        _expectedType = expectedType;
        _delegate = delegate;
    }

    public void add(String id, T object, String... constraints)
    {
        _delegate.add(toQualifiedId(_moduleId, id), validVersionOf(object), constraints);
    }

    private T validVersionOf(T object)
    {
        if (object == null || _expectedType.isInstance(object))
            return object;

        if (!_expectedType.isInstance(object))
            _log.warn(IOCMessages.contributionWrongValueType(_serviceId, _contributionDef, object
                    .getClass(), _expectedType));

        return null;
    }
}
