package ioc.specs

import org.apache.tapestry5.ioc.services.cron.CronSchedule
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CronScheduleSpec extends AbstractSharedRegistrySpecification {

  def "add a job and ensure that it executes"() {
    def latch = new CountDownLatch(5)

    def executor = getService PeriodicExecutor

    executor.addJob(new CronSchedule("0/1 * * * * ?"), "Test", { latch.countDown() })

    when:

    latch.await(30, TimeUnit.SECONDS)

    then:

    latch.count == 0
  }
}
