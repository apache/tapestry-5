// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.commons.internal.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base class for classes that need to manage a ReadWriteLock.
 */
public abstract class LockSupport
{
    private final Lock readLock, writeLock;

    protected LockSupport()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    /**
     * Locks the shared read lock. Any number of threads may lock the read lock at the same time.
     */
    protected final void acquireReadLock()
    {
        readLock.lock();
    }

    /**
     * Takes the exclusive write lock. Once started, no other thread lock the read or write lock. When this method returns,
     * this thread will have locked the write lock and no other thread will have either the read or write lock.
     * Note that this thread must first drop the read lock (if it has it) before attempting to take the write lock, or this method will block forever.
     */
    protected final void takeWriteLock()
    {
        writeLock.lock();
    }

    /**
     * Releases the shared read lock.
     */
    protected final void releaseReadLock()
    {
        readLock.unlock();
    }

    /**
     * Releases the  exclusive read lock.
     */
    protected final void releaseWriteLock()
    {
        writeLock.unlock();
    }

    /**
     * Releases the read lock, then takes the write lock. There's a short window where the thread will have neither lock:
     * during that window, some other thread may have a chance to take the write lock. In code, you'll often see a second check
     * inside the code that has the write lock to see if the update to perform is still necessary.
     */
    protected final void upgradeReadLockToWriteLock()
    {
        releaseReadLock();
        // This is that instant where another thread may grab the write lock. Very rare, but possible.
        takeWriteLock();
    }

    /**
     * Takes the read lock then releases the write lock.
     */
    protected final void downgradeWriteLockToReadLock()
    {
        acquireReadLock();
        releaseWriteLock();
    }
}
