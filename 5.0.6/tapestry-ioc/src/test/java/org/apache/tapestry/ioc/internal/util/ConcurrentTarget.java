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

package org.apache.tapestry.ioc.internal.util;

import java.util.concurrent.TimeUnit;

public class ConcurrentTarget
{
    private final ConcurrentBarrier _barrier = new ConcurrentBarrier();

    private int _counter;

    // Used to check if read locks accumulate when a read lock method calls another read lock method
    public int readCounter()
    {
        return _barrier.withRead(new Invokable<Integer>()
        {
            public Integer invoke()
            {
                return getCounter();
            }
        });
    }

    public int getCounter()
    {
        return _barrier.withRead(new Invokable<Integer>()
        {
            public Integer invoke()
            {
                return _counter;
            }
        });
    }

    public void incrementCounter()
    {
        _barrier.withWrite(new Runnable()
        {
            public void run()
            {
                _counter++;
            }
        });
    }

    public void setCounter(final int counter)
    {
        _barrier.withWrite(new Runnable()
        {
            public void run()
            {
                _counter = counter;
            }
        });
    }

    public void incrementIfNonNegative()
    {
        _barrier.withRead(new Runnable()
        {
            public void run()
            {
                if (_counter >= 0)
                    incrementCounter();
            }
        });
    }

    public void incrementViaRunnable()
    {
        _barrier.withRead(new Runnable()
        {
            public void run()
            {
                Runnable r = new Runnable()
                {
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
        _barrier.withWrite(new Runnable()
        {
            public void run()
            {
                _counter = getCounter() + 1;
            }
        });
    }

    public void tryIncrementCounter()
    {
        _barrier.tryWithWrite(new Runnable()
        {
            public void run()
            {
                _counter++;
            }
        },20, TimeUnit.MILLISECONDS);
    }

    public void tryIncrementCounterHard()
    {
        _barrier.tryWithWrite(new Runnable()
        {
            public void run()
            {
                _counter = getCounter() + 1;
            }
        },20,TimeUnit.MILLISECONDS);
    }

    public void tryIncrementIfNonNegative()
    {
        _barrier.withRead(new Runnable()
        {
            public void run()
            {
                if (_counter >= 0)
                    tryIncrementCounter();
            }
        });
    }


    public void withRead(Runnable runnable ) {
        _barrier.withRead(runnable);        
    }
}
