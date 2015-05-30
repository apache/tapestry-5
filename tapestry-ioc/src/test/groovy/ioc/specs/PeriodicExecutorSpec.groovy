package ioc.specs

import org.apache.tapestry5.ioc.services.cron.CronSchedule;
import org.apache.tapestry5.ioc.services.cron.IntervalSchedule
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor

import spock.lang.Issue;

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock;

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

  @Issue('https://issues.apache.org/jira/browse/TAP5-2455')
  def "One-shot job in the past is not executed"() {

    setup:
    buildRegistry()

    def schedule = new CronSchedule('0 0 0 1 1 ? 2014')

    def jobExecuted = Boolean.FALSE
    when:

    def job = getService(PeriodicExecutor).addJob(schedule, "Job in the past", { jobExecuted = Boolean.TRUE; lock.notifyAll() })

    synchronized (jobExecuted) {
      jobExecuted.wait(2000l)
    }

    then:
    !jobExecuted
    cleanup:

    if (job && !job.isCanceled()){
      job.cancel()
    }

  }
}
