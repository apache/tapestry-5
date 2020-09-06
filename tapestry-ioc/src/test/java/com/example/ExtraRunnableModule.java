package com.example;

import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Startup;

import java.util.List;

public class ExtraRunnableModule
{
    static class InvokeCounts
    {
        static int startupInvokeCount = 0;

        static int contributionInvokeCount = 0;

        static void reset()
        {
            startupInvokeCount = 0;
        }

    }

    @Startup
    public void doStartup()
    {
        InvokeCounts.startupInvokeCount++;
    }

    public ExtraRunnable buildExtraRunnable(final List<Runnable> configuration)
    {
        return new ExtraRunnable()
        {
            @Override
            public void runOrThrow() throws Exception
            {
                run();
            }

            @Override
            public void run()
            {
                for (Runnable r : configuration)
                {
                    r.run();
                }
            }
        };
    }

    public void contributeExtraRunnable(OrderedConfiguration<Runnable> configuration)
    {
        configuration.add("Solo", new Runnable()
        {
            @Override
            public void run()
            {
                InvokeCounts.contributionInvokeCount++;
            }
        });
    }
}
