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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A barrier used to execute code in a context where it is guarded by read/write locks. In addition,
 * handles upgrading read locks to write locks (and vice versa). Execution of code within a lock is
 * in terms of a {@link Runnable} object (that returns no value), or a {@link Invokable} object
 * (which does return a value).
 */
public class ConcurrentBarrier
{
    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    /**
     * This is, of course, a bit of a problem. We don't have an avenue for ensuring that this
     * ThreadLocal is destroyed at the end of the request, and that means a thread can hold a
     * reference to the class and the class loader which loaded it. This may cause redeployment
     * problems (leaked classes and class loaders). Apparently JDK 1.6 provides the APIs to check to
     * see if the current thread has a read lock. So, we tend to remove the TL, rather than set its
     * value to false.
     */
    public static class ThreadBoolean extends ThreadLocal<Boolean>
    {
        @Override
        protected Boolean initialValue()
        {
            return false;
        }
    }

    private final ThreadBoolean _threadHasReadLock = new ThreadBoolean();

    /**
     * Invokes the object after acquiring the read lock (if necessary). If invoked when the read
     * lock has not yet been acquired, then the lock is acquired for the duration of the call. If
     * the lock has already been acquired, then the status of the lock is not changed.
     * <p>
     * TODO: Check to see if the write lock is acquired and <em>not</em> acquire the read lock in
     * that situation. Currently this code is not re-entrant. If a write lock is already acquired
     * and the thread attempts to get the read lock, then the thread will hang. For the moment, all
     * the uses of ConcurrentBarrier are coded in such a way that reentrant locks are not a problem.
     * 
     * @param <T>
     * @param invokable
     * @return the result of invoking the invokable
     */
    public <T> T withRead(Invokable<T> invokable)
    {
        boolean readLockedAtEntry = _threadHasReadLock.get();

        if (!readLockedAtEntry)
        {
            _lock.readLock().lock();

            _threadHasReadLock.set(true);
        }

        try
        {
            return invokable.invoke();
        }
        finally
        {
            if (!readLockedAtEntry)
            {
                _lock.readLock().unlock();

                _threadHasReadLock.remove();
            }
        }
    }

    /**
     * As with {@link #withRead(Invokable)}, creating an {@link Invokable} wrapper around the
     * runnable object.
     */
    public void withRead(final Runnable runnable)
    {
        Invokable<Void> invokable = new Invokable<Void>()
        {
            public Void invoke()
            {
                runnable.run();

                return null;
            }
        };

        withRead(invokable);
    }

    /**
     * Acquires the exclusive write lock before invoking the Invokable. The code will be executed
     * exclusively, no other reader or writer threads will exist (they will be blocked waiting for
     * the lock). If the current thread has a read lock, it is released before attempting to acquire
     * the write lock, and re-acquired after the write lock is released. Note that in that short
     * window, between releasing the read lock and acquiring the write lock, it is entirely possible
     * that some other thread will sneak in and do some work, so the {@link Invokable} object should
     * be prepared for cases where the state has changed slightly, despite holding the read lock.
     * This usually manifests as race conditions where either a) some parallel unrelated bit of work
     * has occured or b) duplicate work has occured. The latter is only problematic if the operation
     * is very expensive.
     * 
     * @param <T>
     * @param invokable
     */
    public <T> T withWrite(Invokable<T> invokable)
    {
        boolean readLockedAtEntry = _threadHasReadLock.get();

        if (readLockedAtEntry)
        {
            _lock.readLock().unlock();

            _threadHasReadLock.set(false);
        }

        _lock.writeLock().lock();

        try
        {
            return invokable.invoke();
        }
        finally
        {
            _lock.writeLock().unlock();

            if (readLockedAtEntry)
            {
                _lock.readLock().lock();

                _threadHasReadLock.set(true);
            }
            else
            {
                _threadHasReadLock.remove();
            }
        }
    }

    /**
     * As with {@link #withWrite(Invokable)}, creating an {@link Invokable} wrapper around the
     * runnable object.
     */
    public void withWrite(final Runnable runnable)
    {
        Invokable<Void> invokable = new Invokable<Void>()
        {
            public Void invoke()
            {
                runnable.run();

                return null;
            }
        };

        withWrite(invokable);
    }
}
