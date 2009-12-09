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

import java.util.concurrent.locks.Lock;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DummyLockTest extends Assert
{
    @Test
    public void dummy_lock_functions_are_noops() throws Exception
    {
        Lock lock = new DummyLock();

        lock.lock();
        lock.unlock();
        lock.lockInterruptibly();

        assertNull(lock.newCondition());
        assertTrue(lock.tryLock());
        assertTrue(lock.tryLock(0, null));
    }
}
