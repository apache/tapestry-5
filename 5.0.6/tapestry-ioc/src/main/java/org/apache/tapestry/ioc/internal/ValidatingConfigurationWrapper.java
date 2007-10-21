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

import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.slf4j.Logger;

/**
 * Performs some validation before delegating to another Configuration.
 */
public class ValidatingConfigurationWrapper<T> implements Configuration<T>
{
    private final String _serviceId;

    private final ContributionDef _contributionDef;

    private final Logger _logger;

    private final Configuration<T> _delegate;

    private final Class _expectedType;

    // Need a strategy for determing the right order for this mass of parameters!

    public ValidatingConfigurationWrapper(String serviceId, Logger logger, Class expectedType,
            ContributionDef contributionDef, Configuration<T> delegate)
    {
        _serviceId = serviceId;
        _logger = logger;
        _expectedType = expectedType;
        _contributionDef = contributionDef;
        _delegate = delegate;
    }

    public void add(T object)
    {
        if (object == null)
        {
            _logger.warn(IOCMessages.contributionWasNull(_serviceId, _contributionDef));
            return;
        }

        // Sure, we say it is type T ... but is it really?

        if (!_expectedType.isInstance(object))
        {
            _logger.warn(IOCMessages.contributionWrongValueType(
                    _serviceId,
                    _contributionDef,
                    object.getClass(),
                    _expectedType));
            return;
        }

        _delegate.add(object);
    }

}
