// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.ioc.services

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.services.cron.IntervalSchedule
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor
import org.apache.tapestry5.ioc.services.cron.PeriodicJob
import org.apache.tapestry5.ioc.test.IOCTestCase
import org.testng.annotations.Test

/**
 * @since 5.3
 */
class PeriodicExecutorTests extends IOCTestCase
{

    @Test
    void execution_intervals()
    {
        Registry r = buildRegistry()

        def countDownLatch = new CountDownLatch(5);

        def schedule = new IntervalSchedule(10)

        PeriodicJob job = r.getService(PeriodicExecutor.class).addJob(schedule, "count incrementer", { countDownLatch.countDown(); })

        countDownLatch.await 30, TimeUnit.SECONDS

        assertEquals countDownLatch.getCount(), 0

        job.cancel()

        r.shutdown()
    }


}
