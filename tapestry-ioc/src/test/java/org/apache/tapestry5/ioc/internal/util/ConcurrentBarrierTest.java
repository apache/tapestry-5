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

package org.apache.tapestry5.ioc.internal.util;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test is structured a bit oddly, since it evolved from when the Concurrence annotation and aspect evolved into the
 * {@link ConcurrentBarrier} utility class.
 */
@Test(sequential = true)
public class ConcurrentBarrierTest extends TestBase
{
    private ConcurrentTarget target = new ConcurrentTarget();

    private static final int THREAD_COUNT = 100;

    private static final int THREAD_BLOCK_SIZE = 5;

    @Test
    public void read_lock_then_write_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                target.incrementCounter();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    @Test
    public void read_lock_inside_write_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                // Gets a write lock, then a read lock.
                target.incrementCounterHard();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    @Test(enabled = true)
    public void write_lock_inside_read_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                // A read lock method that upgrades to a write lock

                target.incrementIfNonNegative();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    @Test(enabled = true)
    public void indirection_between_read_method_and_write_method() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {

                // Read lock method invokes other class, that invokes write method.

                target.incrementViaRunnable();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    /**
     * Test that locking, especially read lock upgrade and downgrade, work properly when there's more than one object
     * involved.
     */
    @Test
    public void multiple_synchronized_objects() throws Exception
    {
        Runnable operation = new ConcurrentTargetWrapper(target);

        runOperationAndCheckCounter(operation);
    }

    @Test
    public void read_lock_then_try_write_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                target.tryIncrementCounter();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    @Test
    public void read_lock_inside_try_write_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                // Gets a write lock, then a read lock.
                target.tryIncrementCounterHard();
            }
        };

        runOperationAndCheckCounter(operation);
    }

    @Test(enabled = true)
    public void try_write_lock_inside_read_lock() throws Exception
    {
        Runnable operation = new Runnable()
        {
            public void run()
            {
                // A read lock method that upgrades to a write lock

                target.tryIncrementIfNonNegative();
            }
        };

        runOperationAndCheckCounter(operation);
    }


    @Test(enabled = true)
    public void write_lock_timeout_inside_read_lock() throws Exception
    {
        final Runnable operation = new Runnable()
        {
            public void run()
            {
                // A read lock method that upgrades to a write lock

                target.tryIncrementIfNonNegative();
            }
        };

        target.withRead(new Runnable()
        {
            public void run()
            {
                try
                {
                    runOperation(operation);
                }
                catch (InterruptedException e)
                {
                }
            }
        });
        assertEquals(target.getCounter(), 0);

    }


    private void runOperationAndCheckCounter(Runnable operation) throws InterruptedException
    {
        runOperation(operation);

        assertEquals(target.getCounter(), THREAD_COUNT);
    }

    private void runOperation(Runnable operation)
            throws InterruptedException
    {
        List<Thread> threads = newList();
        List<Thread> running = newList();

        target.setCounter(0);

        for (int i = 0; i < THREAD_COUNT; i++)
        {

            Thread t = new Thread(operation);

            threads.add(t);

            if (threads.size() >= THREAD_BLOCK_SIZE)
                startThreads(threads, running);
        }

        startThreads(threads, running);

        for (Thread t : running)
            t.join();
    }

    private void startThreads(List<Thread> threads, List<Thread> running)
    {
        for (Thread t : threads)
        {
            t.start();
            running.add(t);
        }

        threads.clear();
    }

}
