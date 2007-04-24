// Copyright 2007 The Apache Software Foundation
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

import java.util.Map;

import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategySource;

public class ApplicationStatePersistenceStrategySourceImpl implements
        ApplicationStatePersistenceStrategySource
{
    private final Map<String, ApplicationStatePersistenceStrategy> _configuration;

    public ApplicationStatePersistenceStrategySourceImpl(
            Map<String, ApplicationStatePersistenceStrategy> configuration)
    {
        _configuration = configuration;
    }

    public ApplicationStatePersistenceStrategy get(String name)
    {
        ApplicationStatePersistenceStrategy result = _configuration.get(name);

        if (result == null)
            throw new RuntimeException(ServicesMessages.missingApplicationStatePersistenceStrategy(
                    name,
                    _configuration.keySet()));

        return result;
    }
}
