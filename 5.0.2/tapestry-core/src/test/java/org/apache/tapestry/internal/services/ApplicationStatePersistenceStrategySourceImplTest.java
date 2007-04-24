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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategySource;
import org.testng.annotations.Test;

public class ApplicationStatePersistenceStrategySourceImplTest extends InternalBaseTestCase
{
    @Test
    public void strategy_found()
    {
        ApplicationStatePersistenceStrategy strategy = newApplicationStatePersistenceStrategy();

        Map<String, ApplicationStatePersistenceStrategy> configuration = Collections.singletonMap(
                "session",
                strategy);

        replay();

        ApplicationStatePersistenceStrategySource source = new ApplicationStatePersistenceStrategySourceImpl(
                configuration);

        assertSame(strategy, source.get("session"));

        verify();
    }

    @Test
    public void not_found()
    {
        ApplicationStatePersistenceStrategy strategy = newApplicationStatePersistenceStrategy();

        Map<String, ApplicationStatePersistenceStrategy> configuration = newMap();

        configuration.put("session", strategy);
        configuration.put("application", strategy);

        replay();

        ApplicationStatePersistenceStrategySource source = new ApplicationStatePersistenceStrategySourceImpl(
                configuration);

        try
        {
            source.get("aether");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No application state persistence strategy is available with name 'aether'. Available strategies: application, session.");
        }

        verify();

    }
}
