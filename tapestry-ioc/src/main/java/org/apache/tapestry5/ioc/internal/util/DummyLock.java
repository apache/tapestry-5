// Copyright 2009 The Apache Software Foundation
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Acts like a Lock but all operations are no-ops.
 * @deprecated Deprecated in 5.4 with no replacement.
 */
public class DummyLock implements Lock
{
    @Override
    public void lock()
    {
    }

    @Override
    public void lockInterruptibly() throws InterruptedException
    {
    }

    /**
     * Returns null.
     */
    @Override
    public Condition newCondition()
    {
        return null;
    }

    /** @return true */
    @Override
    public boolean tryLock()
    {
        return true;
    }

    /** @return true */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
    {
        return true;
    }

    @Override
    public void unlock()
    {
    }

}
