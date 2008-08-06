// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.NullFieldStrategy;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.NullFieldStrategySource;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class NullFieldStrategySourceImplTest extends InternalBaseTestCase
{
    @Test
    public void success()
    {
        NullFieldStrategy strategy = mockNullFieldStrategy();

        replay();

        Map<String, NullFieldStrategy> configuration = Collections.singletonMap("strat", strategy);

        NullFieldStrategySource source = new NullFieldStrategySourceImpl(configuration);

        assertSame(source.get("strat"), strategy);

        verify();
    }

    @Test
    public void failure()
    {
        Map<String, NullFieldStrategy> configuration = CollectionFactory.newCaseInsensitiveMap();

        configuration.put("fred", mockNullFieldStrategy());
        configuration.put("barney", mockNullFieldStrategy());

        replay();

        NullFieldStrategySource source = new NullFieldStrategySourceImpl(configuration);

        try
        {
            source.get("wilma");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Unrecognized name 'wilma' locating a null field strategy.  Available strategies: barney, fred.");
        }

    }
}
