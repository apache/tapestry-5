// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test.internal.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.internal.util.ConcurrentBarrier;

import java.util.concurrent.TimeUnit;

public class ConcurrentTarget
{
    private final ConcurrentBarrier barrier = new ConcurrentBarrier();

    private int counter;

    // Used to check if read locks accumulate when a read lock method calls another read lock method
    public int readCounter()
    {
        return barrier.withRead(new Invokable<Integer>()
        {
            @Override
            public Integer invoke()
            {
                return getCounter();
            }
        });
    }

    public int getCounter()
    {
        return barrier.withRead(new Invokable<Integer>()
        {
            @Override
            public Integer invoke()
            {
                return counter;
            }
        });
    }

    public void incrementCounter()
    {
        barrier.withWrite(new Runnable()
        {
            @Override
            public void run()
            {
                counter++;
            }
        });
    }

    public void setCounter(final int counter)
    {
        barrier.withWrite(new Runnable()
        {
            @Override
            public void run()
            {
                ConcurrentTarget.this.counter = counter;
            }
        });
    }

    public void incrementIfNonNegative()
    {
        barrier.withRead(new Runnable()
        {
            @Override
            public void run()
            {
                if (counter >= 0)
                    incrementCounter();
            }
        });
    }

    public void incrementViaRunnable()
    {
        barrier.withRead(new Runnable()
        {
            @Override
            public void run()
            {
                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        incrementCounter();
                    }
                };

                r.run();
            }
        });
    }

    public void incrementCounterHard()
    {
        barrier.withWrite(new Runnable()
        {
            @Override
            public void run()
            {
                counter = getCounter() + 1;
            }
        });
    }

    public boolean tryIncrementCounter()
    {
        return barrier.tryWithWrite(new Runnable()
        {
            @Override
            public void run()
            {
                counter++;
            }
        }, 20, TimeUnit.MILLISECONDS);
    }

    public boolean tryIncrementCounterHard()
    {
        return barrier.tryWithWrite(new Runnable()
        {
            @Override
            public void run()
            {
                counter = getCounter() + 1;
            }
        }, 20, TimeUnit.MILLISECONDS);
    }

    public boolean tryIncrementIfNonNegative()
    {
        final List<Boolean> result = new ArrayList<Boolean>();
        barrier.withRead(new Runnable()
        {
            @Override
            public void run()
            {
                if (counter >= 0)
                    result.add(tryIncrementCounter());
            }
        });
        return result.get(0);
    }


    public void withRead(Runnable runnable)
    {
        barrier.withRead(runnable);
    }
}
