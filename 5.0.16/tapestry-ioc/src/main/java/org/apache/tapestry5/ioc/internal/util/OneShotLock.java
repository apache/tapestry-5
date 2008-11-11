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

/**
 * Logic for handling one shot semantics for classes; classes that include a method (or methods) that "locks down" the
 * instance, to prevent it from being used again in the future.
 */
public class OneShotLock
{
    private boolean lock;

    /**
     * Checks to see if the lock has been set (via {@link #lock()}).
     *
     * @throws IllegalStateException if the lock is set
     */
    public synchronized void check()
    {
        innerCheck();
    }

    private void innerCheck()
    {
        if (lock)
        {
            // This is how I would think it would be:

            // [0] is getStackTrace()
            // [1] is innerCheck()
            // [2] is check() or lock()
            // [3] is caller of check() or lock()

            // ... so why do we get element 4?  Found this via trial and error.  Some extra stack frame
            // gets in there somehow, as in, getStackTrace() must be calling something (probably native)
            // that creates the actual array, and includes itself as [0], getStackTrace() as [1], etc.
            // Maybe it's something to do with synchronized?

            StackTraceElement element = Thread.currentThread().getStackTrace()[4];

            throw new IllegalStateException(UtilMessages.oneShotLock(element));
        }
    }

    /**
     * Checks the lock, then sets it.
     */
    public synchronized void lock()
    {
        innerCheck();

        lock = true;
    }
}
