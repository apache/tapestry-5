package ioc.specs

import org.apache.tapestry5.ioc.internal.services.RegistryShutdownHubImpl
import org.apache.tapestry5.ioc.services.RegistryShutdownListener
import org.slf4j.Logger
import spock.lang.Specification

class RegistryshutdownHubImplSpec extends Specification {

  RegistryShutdownHubImpl hub
  Logger logger = Mock()

  def setup() {
    hub = new RegistryShutdownHubImpl(logger)
  }

  def "add old-style listeners and verify order"() {
    RegistryShutdownListener l1 = Mock()
    RegistryShutdownListener l2 = Mock()

    when:

    hub.addRegistryShutdownListener l1
    hub.addRegistryShutdownListener l2

    then:

    0 * _

    when:

    hub.fireRegistryDidShutdown()

    then:

    1 * l1.registryDidShutdown()

    then:

    1 * l2.registryDidShutdown()
    0 * _
  }

  def "will-shutdown-listeners are invoked before normal shutdown listeners"() {
    Runnable will1 = Mock()
    Runnable will2 = Mock()

    RegistryShutdownListener l1 = Mock()
    RegistryShutdownListener l2 = Mock()

    hub.addRegistryWillShutdownListener will1
    hub.addRegistryWillShutdownListener will2

    hub.addRegistryShutdownListener l1
    hub.addRegistryShutdownListener l2

    when:

    hub.fireRegistryDidShutdown()

    then:

    1 * will1.run()

    then:

    1 * will2.run()

    then:

    1 * l1.registryDidShutdown()
    1 * l2.registryDidShutdown()
    0 * _
  }

  def "an exception during notification is logged and notification continues"() {
    Runnable l1 = Mock()
    Runnable l2 = Mock()

    hub.addRegistryShutdownListener l1
    hub.addRegistryShutdownListener l2

    RuntimeException e = new RuntimeException("Failure.")

    when:

    hub.fireRegistryDidShutdown()

    then:

    1 * l1.run() >> { throw e }
    1 * logger.error(_, _) >> { message, exception ->
      ["Error notifying", "registry shutdown", "Failure"].each {
        assert message.contains(it)
      }

      assert exception.is(e)
    }

    then:

    1 * l2.run()
    0 * _
  }

}
