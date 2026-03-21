// Copyright 2026 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.test.internal.util.ConcurrentTarget;
import org.apache.tapestry5.ioc.test.internal.util.ConcurrentTargetWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spock.lang.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentBarrierTest {

    private ConcurrentTarget target;

    private static final int THREAD_COUNT = 1000;
    private static final int THREAD_BLOCK_SIZE = 50;

    @BeforeEach
    void beforeEach() {
        target = new ConcurrentTarget();
    }

     /**
     * Helper method to execute a runnable across 1000 threads, 
     * batched and started in blocks of 50.
     */
    private void run(Runnable op) throws InterruptedException {
        assertEquals(0, target.getCounter(), "Counter should initially be 0");

        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        List<Thread> running = new ArrayList<>(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread t = new Thread(op);
            threads.add(t);

            if (threads.size() >= THREAD_BLOCK_SIZE) {
                for (Thread thread : threads) {
                    thread.start();
                }
                running.addAll(threads);
                threads.clear();
            }
        }

        for (Thread thread : running) {
            thread.join();
        }
    }

      @Test
    void acquireWriteLock() throws InterruptedException {
        run(target::incrementCounter);

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void acquireReadLockWhileHoldingWriteLock() throws InterruptedException {
        run(target::incrementCounterHard);

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void upgradeReadLockToWriteLock() throws InterruptedException {
        run(target::incrementIfNonNegative);

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void indirectionBetweenMethodWithReadLockAndMethodThatAcquiresWriteLock() throws InterruptedException {
        run(target::incrementViaRunnable);

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void barriersAreIndependentWhenMultipleAreInvolved() throws InterruptedException {
        run(new ConcurrentTargetWrapper(target));

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void useTryWithWriteToGetWriteLockIfItIsAvailable() throws InterruptedException {
        run(() -> {
            boolean good = false;
            while (!good) {
                good = target.tryIncrementCounter();
            }
        });

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void acquireReadLockWhenInsideATryWithWriteBlock() throws InterruptedException {
        run(() -> {
            boolean good = false;
            while (!good) {
                good = target.tryIncrementCounterHard();
            }
        });

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void readLockUpgradesViaTryWriteLock() throws InterruptedException {
        run(() -> {
            boolean good = false;
            while (!good) {
                good = target.tryIncrementIfNonNegative();
            }
        });

        assertEquals(THREAD_COUNT, target.getCounter());
    }

    @Test
    void writeLockTimeoutInsideReadLock() {
        target.withRead(() -> {
            try {
                run(() -> {
                    // With a read lock already held by the main thread, 
                    // the background threads will fail to get the write lock.
                    assertFalse(target.tryIncrementIfNonNegative());
                });
            } catch (InterruptedException e) {
                // Restore interrupt status as best practice
                Thread.currentThread().interrupt();
            }
        });

        assertEquals(0, target.getCounter());
    }

    @Issue("TAP5-2820")
    @Test
    void testInterruptedExceptionIsNotSwallowed() throws InterruptedException {

        ConcurrentBarrier barrier = new ConcurrentBarrier();
        AtomicBoolean threadWasInterrupted = new AtomicBoolean(false);
        CountDownLatch backgroundThreadBlocked = new CountDownLatch(1);

        // 1. Main thread takes the write lock
        barrier.withWrite(() -> {
            Thread backgroundThread = new Thread(() -> {
                backgroundThreadBlocked.countDown();
                // 2. Background thread attempts to acquire write lock and blocks
                barrier.tryWithWrite(() -> {}, 10, TimeUnit.SECONDS);
                
                // 3. Check if the interrupt flag was safely restored after the catch block
                threadWasInterrupted.set(Thread.currentThread().isInterrupted());
            });

            backgroundThread.start();
            try {
                backgroundThreadBlocked.await();
                Thread.sleep(100); // Ensure it is waiting inside tryLock()
                
                // 4. Send the interrupt signal
                backgroundThread.interrupt();
                backgroundThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });

        assertTrue(threadWasInterrupted.get(), "Thread interrupt status was swallowed and lost!");
    }

    @Issue("TAP5-2820")
    @Test
    void testHiddenBarrierLivelock() throws InterruptedException {

        // Previously, the ConcurrenBarrier was synchronized on a ThreadLocal monitor,
        // which could cause a livelock/starvation scenario under heavy contention.

        ConcurrentBarrier barrier = new ConcurrentBarrier();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        Runnable task = () -> {
            try {
                startLatch.await(); // Start all threads at the exact same time
                
                boolean good = false;
                int attempts = 0;
                // Simulate the while(!good) livelock scenario
                while (!good && attempts < 100) {
                    good = barrier.tryWithWrite(() -> {}, 1, TimeUnit.MILLISECONDS);
                    attempts++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(task);
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // Unleash the threads

        // Wait for threads to finish, with a max timeout
        boolean finishedInTime = doneLatch.await(10, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        executor.shutdownNow();

        assertTrue(finishedInTime, "Livelock detected! Threads took too long to complete: " + duration + "ms");
    }
}