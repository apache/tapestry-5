package org.apache.tapestry.ioc.services

import org.apache.tapestry.ioc.AbstractRegistrySpecification
import org.apache.tapestry5.ioc.services.cron.IntervalSchedule
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PeriodicExecutorSpec extends AbstractRegistrySpecification {

    def "execution intervals"() {

        buildRegistry()

        def countDownLatch = new CountDownLatch(5);

        def schedule = new IntervalSchedule(10)

        def job = getService(PeriodicExecutor).addJob(schedule, "count incrementer", { countDownLatch.countDown(); })

        countDownLatch.await 30, TimeUnit.SECONDS

        cleanup:

        job && job.cancel()


    }
}
