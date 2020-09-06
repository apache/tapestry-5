package ioc.specs

import org.apache.tapestry5.ioc.test.internal.util.ConcurrentTarget
import org.apache.tapestry5.ioc.test.internal.util.ConcurrentTargetWrapper

import spock.lang.Specification

class ConcurrentBarrierSpec extends Specification {

  def target = new ConcurrentTarget()

  static final int THREAD_COUNT = 1000

  static final int THREAD_BLOCK_SIZE = 50

  def run(op) {
    def threads = []
    def running = []

    assert target.counter == 0

    THREAD_COUNT.times {
      def t = new Thread(op)

      threads << t

      if (threads.size() >= THREAD_BLOCK_SIZE) {
        threads.each { it.start() }
        running.addAll threads
        threads.clear()
      }
    }

    running.each { it.join() }
  }

  def "acquire write lock"() {

    when:

    run { target.incrementCounter() }

    then:

    target.counter == THREAD_COUNT
  }

  def "acquire read lock while holding write lock"() {

    when:

    run { target.incrementCounterHard() }

    then:

    target.counter == THREAD_COUNT
  }

  def "upgrade read lock to write lock"() {
    when:

    run { target.incrementIfNonNegative() }

    then:

    target.counter == THREAD_COUNT
  }

  def "indirection between method with read lock and method that acquires write lock"() {

    when:

    run { target.incrementViaRunnable() }

    then:

    target.counter == THREAD_COUNT
  }

  def "barriers are independent when multiple are involved"() {

    when:

    run(new ConcurrentTargetWrapper(target))

    then:

    target.counter == THREAD_COUNT
  }

  def "use tryWithWrite() to get write lock if it is available"() {

    when: run {
      def good = false
      while (!good) { good = target.tryIncrementCounter() }
    }

    then:

    target.counter == THREAD_COUNT
  }

  def "acquire read lock when inside a tryWithWrite block"() {

    when:

    run {
      def good = false
      while (!good) { good = target.tryIncrementCounterHard() }
    }

    then:

    target.counter == THREAD_COUNT
  }

  def "read lock upgrades via tryWriteLock()"() {

    when:

    run {
      def good = false
      while (!good) { good = target.tryIncrementIfNonNegative() }
    }

    then:

    target.counter == THREAD_COUNT
  }

  def "write lock timeout inside read lock"() {
    when:

    target.withRead {
      try {
        run {
          assert target.tryIncrementIfNonNegative() == false
        }
      }
      catch (InterruptedException e) { }
    }

    then:

    target.counter == 0
  }
}
