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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource;

import java.util.Map;

public class ApplicationStatePersistenceStrategySourceImpl implements
        ApplicationStatePersistenceStrategySource
{
    private final Map<String, ApplicationStatePersistenceStrategy> configuration;

    public ApplicationStatePersistenceStrategySourceImpl(Map<String, ApplicationStatePersistenceStrategy> configuration)
    {
        this.configuration = configuration;
    }

    public ApplicationStatePersistenceStrategy get(String name)
    {
        ApplicationStatePersistenceStrategy result = configuration.get(name);

        if (result == null)
            throw new RuntimeException(ServicesMessages.missingApplicationStatePersistenceStrategy(
                    name,
                    configuration.keySet()));

        return result;
    }
}
