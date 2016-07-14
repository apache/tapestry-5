package ioc.specs

import org.apache.tapestry5.ioc.Invokable
import org.apache.tapestry5.ioc.internal.services.PerthreadManagerImpl
import org.apache.tapestry5.ioc.services.ThreadCleanupListener
import org.slf4j.Logger
import spock.lang.Specification

class PerthreadManagerImplSpec extends Specification {

  def "nothing is logged when cleaning up with no listeners"() {
    Logger logger = Mock()

    def manager = new PerthreadManagerImpl(logger)

    when:

    manager.cleanup()

    then:

    0 * _
  }

  def "listeners will only be invoked a single time, then discarded"() {
    Logger logger = Mock()
    ThreadCleanupListener listener = Mock()

    def manager = new PerthreadManagerImpl(logger)

    when:

    manager.addThreadCleanupListener(listener)
    manager.cleanup()

    then:

    1 * listener.threadDidCleanup()
    0 * _

    when:

    manager.cleanup()

    then:

    0 * _
  }

  def "exceptions during thread cleanup are logged and other listeners still invoked"() {
    RuntimeException t = new RuntimeException("Boom!")
    Logger logger = Mock()
    ThreadCleanupListener l1 = Mock()
    ThreadCleanupListener l2 = Mock()

    def manager = new PerthreadManagerImpl(logger)

    manager.addThreadCleanupListener(l1)
    manager.addThreadCleanupListener(l2)

    when:

    manager.cleanup()

    then:

    1 * l1.threadDidCleanup() >> { throw t }
    1 * logger.warn("Error invoking callback {}: {}", {it instanceof Runnable}, t, t)

    then:

    1 * l2.threadDidCleanup()
    0 * _
  }

  def "PerThreadValue does not initially exist"() {
    Logger logger = Mock()
    def manager = new PerthreadManagerImpl(logger)

    when:

    def value = manager.createValue()

    then:

    !value.exists()
    value.get() == null

    when:

    value.set(this)

    then:

    value.exists()
    value.get() == this
  }

  def "PerThreadValue.get() with default returns the default value when the value does not exist"() {
    Logger logger = Mock()
    def manager = new PerthreadManagerImpl(logger)
    def defaultValue = new Object()
    def nonNull = new Object()

    when:

    def value = manager.createValue()

    then:

    value.get(defaultValue).is(defaultValue)

    when:

    value.set(null)

    then:

    value.exists()
    value.get(defaultValue) == null

    when:

    value.set(nonNull)

    then:

    value.get(defaultValue).is(nonNull)
  }

  def "PerthreadManager.run() performs an implicit cleanup"() {
    Logger logger = Mock()
    ThreadCleanupListener listener = Mock()

    def manager = new PerthreadManagerImpl(logger)
    manager.addThreadCleanupListener listener
    def value = manager.createValue()
    def didRun = false

    def runnable = {
      didRun = true
      value.set "bar"
    }

    when:

    manager.run runnable

    then:

    1 * listener.threadDidCleanup()

    didRun
    !value.exists()
  }

  def "PerthreadManager.invoke() performs an implicit cleanup"() {
    Logger logger = Mock()
    ThreadCleanupListener listener = Mock()

    def manager = new PerthreadManagerImpl(logger)
    manager.addThreadCleanupListener listener
    def value = manager.createValue()

    def inv = {
      value.set "bar"
      return "baz"
    } as Invokable

    when:

    assert manager.invoke(inv) == "baz"

    then:

    1 * listener.threadDidCleanup()

    !value.exists()

  }


}
