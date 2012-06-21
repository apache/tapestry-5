package ioc.specs

import org.apache.tapestry5.ioc.internal.util.DummyLock
import spock.lang.Specification

import java.util.concurrent.locks.Lock

class DummyLockSpec extends Specification {

  def "all methods are no-ops"() {
    Lock lock = new DummyLock()

    when:

    lock.lock()
    lock.unlock()
    lock.lockInterruptibly()

    then:

    noExceptionThrown()

    expect:
    lock.newCondition() == null
    lock.tryLock()
    lock.tryLock(0, null)
  }
}
