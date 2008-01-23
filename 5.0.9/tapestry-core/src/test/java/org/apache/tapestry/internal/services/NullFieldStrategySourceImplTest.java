package org.apache.tapestry.internal.services;

import org.apache.tapestry.NullFieldStrategy;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.NullFieldStrategySource;
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

    protected final NullFieldStrategy mockNullFieldStrategy()
    {
        return newMock(NullFieldStrategy.class);
    }
}
