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

import org.apache.tapestry5.ioc.Invokable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A barrier used to execute code in a context where it is guarded by read/write locks. In addition, handles upgrading
 * read locks to write locks (and vice versa). Execution of code within a lock is in terms of a {@link Runnable} object
 * (that returns no value), or a {@link Invokable} object (which does return a value).
 */
public class ConcurrentBarrier
{
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Invokes the object after acquiring the read lock (if necessary). If invoked when the read lock has not yet been
     * acquired, then the lock is acquired for the duration of the call. If the lock has already been acquired, then the
     * status of the lock is not changed.
     * <p>
     * This method is completely re-entrant. If a write lock is already acquired by the current thread, 
     * this method gracefully bypasses acquiring the read lock to prevent deadlocks.
     *
     * @param <T>       the type of the return value
     * @param invokable the code to execute safely inside the read lock
     * @return the result of invoking the invokable
     */
    public <T> T withRead(Invokable<T> invokable)
    {
        // If the current thread already holds a read lock, OR if it holds the exclusive write lock,
        // it implicitly has read access. We can safely bypass acquiring the read lock again.
        boolean readLockedAtEntry = lock.getReadHoldCount() > 0 || lock.isWriteLockedByCurrentThread();

        if (!readLockedAtEntry)
        {
            lock.readLock().lock();
        }

        try
        {
            return invokable.invoke();
        }
        finally
        {
            // Only unlock if we were the ones who actually locked it in this method call.
            if (!readLockedAtEntry)
            {
                lock.readLock().unlock();
            }
        }
    }

    /**
     * As with {@link #withRead(Invokable)}, creating an {@link Invokable} wrapper around the runnable object.
     */
    public void withRead(final Runnable runnable)
    {
        withRead((Invokable<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Acquires the exclusive write lock before invoking the Invokable. The code will be executed exclusively, no other
     * reader or writer threads will exist (they will be blocked waiting for the lock).
     * <p>
     * If the current thread has a read lock, it is released before attempting to acquire the write lock, and
     * re-acquired after the write lock is released. Note that in that short window, between releasing the read lock
     * and acquiring the write lock, it is entirely possible that some other thread will sneak in and do some work,
     * so the {@link Invokable} object should be prepared for cases where the state has changed slightly, despite
     * holding the read lock. This usually manifests as race conditions where either a) some parallel unrelated
     * bit of work has occured or b) duplicate work has occured. The latter is only problematic if the operation
     * is very expensive.
     *
     * @param <T>       the type of the return value
     * @param invokable the code to execute safely inside the write lock
     * @return the result of invoking the invokable
     */
    public <T> T withWrite(Invokable<T> invokable)
    {
        boolean readLockedAtEntry = lock.getReadHoldCount() > 0;

        // ReentrantReadWriteLock does NOT allow upgrading a read lock directly to a write lock.
        // If we try to get the write lock while holding a read lock, the thread will deadlock forever.
        // Therefore, we MUST release our read lock first before asking for the write lock.
        if (readLockedAtEntry)
        {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();

        try
        {
            return invokable.invoke();
        }
        finally
        {
            // Always release the write lock when done
            lock.writeLock().unlock();

            // If we released a read lock at the start of this method, we must restore it here
            // so the calling code doesn't break when it assumes it still has a read lock.
            if (readLockedAtEntry)
            {
                restoreReadLock();
            }
        }
    }

    /**
     * As with {@link #withWrite(Invokable)}, creating an {@link Invokable} wrapper around the runnable object.
     */
    public void withWrite(final Runnable runnable)
    {
        withWrite((Invokable<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Try to acquire the exclusive write lock and invoke the Runnable. If the write lock is obtained within the specified
     * timeout, then this method behaves as {@link #withWrite(Runnable)} and will return true. If the write lock is not
     * obtained within the timeout then the runnable is never invoked and the method will return false.
     *
     * @param runnable    Runnable object to execute inside the write lock.
     * @param timeout     Time to wait for write lock.
     * @param timeoutUnit Units of timeout.
     * @return true if lock was obtained and the runnable executed, or false otherwise.
     */
    public boolean tryWithWrite(final Runnable runnable, long timeout, TimeUnit timeoutUnit)
    {
         boolean readLockedAtEntry = lock.getReadHoldCount() > 0;

         // Just like withWrite(), we must temporarily release any read lock we hold
        // so we don't deadlock while waiting for the write lock.
        if (readLockedAtEntry)
        {
            lock.readLock().unlock();
        }

        boolean obtainedLock = false;

        try {
            try {
                obtainedLock = lock.writeLock().tryLock(timeout, timeoutUnit);
                if (obtainedLock)
                {
                    runnable.run();
                }
            }
            catch (InterruptedException e)
            {
                obtainedLock = false;

                // We MUST re-interrupt the thread here so the system knows the thread was asked to stop.
                Thread.currentThread().interrupt(); 
            }
            finally
            {
                // Only unlock the write lock if we actually succeeded in getting it
                if (obtainedLock)
                {
                    lock.writeLock().unlock();
                }
            }
        }
        finally
        {
            // Ensure that no matter what happened (timeout, interrupt, error), 
            // if the thread came in with a read lock, it leaves with a read lock.
            if (readLockedAtEntry)
            {
                restoreReadLock();
            }
        }

        return obtainedLock;
    }

    /**
     * Helper to safely re-acquire the read lock after it was temporarily dropped.
     * Uses a generous timeout to prevent unbounded reader starvation if a massive
     * queue of writers is pending.
     */
    private void restoreReadLock() 
    {
        try 
        {
            // Give it 30 seconds to re-acquire to prevent infinite starvation.
            if (!lock.readLock().tryLock(30, TimeUnit.SECONDS)) 
            {
                throw new IllegalStateException("Unable to re-acquire read lock after 30 seconds due to writer starvation.");
            }
        } 
        catch (InterruptedException e) 
        {
            // The thread was interrupted while waiting to get its read lock back.
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while attempting to re-acquire read lock.", e);
        }
    }
}
