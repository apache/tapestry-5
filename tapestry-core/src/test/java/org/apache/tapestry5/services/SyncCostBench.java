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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.internal.util.ConcurrentBarrier;

import static java.lang.String.format;
import static java.lang.System.out;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Tests single-thread synchronization overhead using different techniques. Note that we're fudging things a bit by
 * getting a read lock for a write operation .... it's just that I'm more concerned about read locks (which will be very
 * common) than about write locks (very rare). Another concern is that hotspot is going to mess up our synchronization
 * when it see we're not really doing anything multi-threaded.
 * <p/>
 * The results show that using the {@link org.apache.tapestry5.internal.annotations.Concurrent} aspect (which used a
 * {@link java.util.concurrent.locks.ReentrantReadWriteLock} under the covers) is about 4x as expensive as just using
 * the synchronized keyword. There are some anomolous results ... for example, ReadWriteLockRunner is consistently
 * slower than ReadWriteLockAspectRunner (one would expect it to be the other way around ... must be something about how
 * AspectJ weaves the code ... and it's use of static methods in many cases).
 * <p/>
 * Well, the Concurrent aspect is gone, replaced with the {@link ConcurrentBarrier} utility.
 */
public class SyncCostBench
{
    /**
     * Calculates a fibunacci series.
     */
    static class Worker implements Runnable
    {
        private long[] series = { 1, 1 };

        public void run()
        {
            long value = series[0] + series[1];

            // Now shift the values down to prepare for the next iteration.

            series[0] = series[1];
            series[1] = value;
        }
    }

    static class SimpleRunner implements Runnable
    {
        private final Runnable delegate;

        public SimpleRunner(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public void run()
        {
            delegate.run();
        }
    }

    static class SynchronizedRunner implements Runnable
    {
        private final Runnable delegate;

        public SynchronizedRunner(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public synchronized void run()
        {
            delegate.run();
        }
    }

    static class ReadWriteLockAspectRunner implements Runnable
    {
        private final ConcurrentBarrier barrier = new ConcurrentBarrier();

        private final Runnable delegate;

        public ReadWriteLockAspectRunner(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public void run()
        {
            barrier.withRead(delegate);
        }
    }

    static class ReadWriteLockRunner implements Runnable
    {
        private final Runnable delegate;

        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public ReadWriteLockRunner(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public void run()
        {

            try
            {
                lock.readLock().lock();

                delegate.run();
            }
            finally
            {
                lock.readLock().unlock();
            }

        }
    }

    private static final int WARMUP_BLOCK_SIZE = 1000;

    private static final int BLOCK_SIZE = 5 * 1000 * 1000;

    static class BlockRunner implements Runnable
    {
        private final Runnable delegate;

        private final int blockSize;

        public BlockRunner(int blockSize, Runnable delegate)
        {
            this.blockSize = blockSize;
            this.delegate = delegate;
        }

        public void run()
        {
            for (int i = 0; i < blockSize; i++)
                delegate.run();
        }
    }

    public static void main(String[] args) throws Exception
    {
        Runnable simple = new SimpleRunner(new Worker());
        Runnable synched = new SynchronizedRunner(new Worker());
        Runnable rw = new ReadWriteLockRunner(new Worker());
        Runnable aspect = new ReadWriteLockAspectRunner(new Worker());

        out.println(format("%40s %9s %9s %9s", ",simple", ",synched", ",rw", ",aspect"));

        stage("warmup");

        go(WARMUP_BLOCK_SIZE, simple);
        go(WARMUP_BLOCK_SIZE, synched);
        go(WARMUP_BLOCK_SIZE, rw);
        go(WARMUP_BLOCK_SIZE, aspect);

        out.println();

        for (int i = 0; i < 10; i++)
        {
            Thread.sleep(5 * 1000);
            System.gc();

            stage(format("stage #%d", i + 1));
            go(BLOCK_SIZE, simple);
            go(BLOCK_SIZE, synched);
            go(BLOCK_SIZE, rw);
            go(BLOCK_SIZE, aspect);

            out.println();
        }
    }

    private static void stage(String name)
    {
        out.print(format("%30s", name));
    }

    private static void go(int blockSize, Runnable runner) throws InterruptedException
    {

        Thread t = new Thread(new BlockRunner(blockSize, runner));

        long tick = System.nanoTime();

        t.start();

        // Now wait for it to finish.

        t.join();

        long tock = System.nanoTime();

        out.print(format(",%9d", tock - tick));
    }
}
