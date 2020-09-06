// Copyright 2006, 2007, 2008, 2009, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.internal.util.LockSupport;

/**
 * Logic for handling one shot semantics for classes; classes that include a method (or methods) that "locks down" the
 * instance, to prevent it from being used again in the future.
 */
public class OneShotLock extends LockSupport
{
    private boolean lock;

    /**
     * Checks to see if the lock has been set (via {@link #lock()}).
     *
     * @throws IllegalStateException
     *         if the lock is set
     */
    public void check()
    {
        try
        {
            acquireReadLock();

            innerCheck();
        } finally
        {
            releaseReadLock();
        }
    }

    private void innerCheck()
    {
        if (lock)
        {
            // The depth to find the caller of the check() or lock() method varies between JDKs.

            StackTraceElement[] elements = Thread.currentThread().getStackTrace();

            int i = 0;
            while (!elements[i].getMethodName().equals("innerCheck"))
            {
                i++;
            }

            throw new IllegalStateException(UtilMessages.oneShotLock(elements[i + 2]));
        }
    }

    /**
     * Checks the lock, then sets it.
     */
    public void lock()
    {
        try
        {
            takeWriteLock();

            innerCheck();

            lock = true;
        } finally
        {
            releaseWriteLock();
        }
    }
}
