//  Copyright 2013 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.apache.tapestry5.http.internal.services;

/**
 * A wrapper around {@link java.util.concurrent.locks.ReentrantReadWriteLock} used to manage the lock for a session.
 * Once a lock is acquired, a callback is registered with the {@link org.apache.tapestry5.ioc.services.PerthreadManager}
 * to release the lock at the end of the request.
 *
 * @since 5.4
 */
public interface SessionLock
{
    /**
     * Acquires the read lock, if the shared read lock, or exclusive write lock, is not already held by this thread.
     */
    void acquireReadLock();

    /**
     * Acquires the exclusive write lock; may release the (shared) read lock before acquiring the write lock;
     * this may block for a while. Does nothing if the write lock is already held by this thread.
     */
    void acquireWriteLock();
}
