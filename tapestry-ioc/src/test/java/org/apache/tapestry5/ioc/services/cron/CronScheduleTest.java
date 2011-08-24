// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services.cron;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CronScheduleTest extends IOCInternalTestCase
{

    @Test
    public void schedule() throws InterruptedException
    {
        final CountDownLatch countDownLatch = new CountDownLatch(5);

        final PeriodicExecutor executor = getService(PeriodicExecutor.class);

        executor.addJob(new CronSchedule("0/1 * * * * ?"), "Test", new Runnable()
        {
            public void run()
            {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await(30, TimeUnit.SECONDS);

        assertEquals(countDownLatch.getCount(), 0);
    }
}
