package ioc.specs

import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.internal.services.RegistryStartup
import org.apache.tapestry5.ioc.test.internal.services.StartupModule
import org.slf4j.Logger

import spock.lang.Specification

class RegistryStartupSpec extends Specification {


  def "ensure that RegistryStartup service runs each of its contributed callbacks"() {
    Runnable r1 = Mock()
    Runnable r2 = Mock()
    Logger logger = Mock()
    def configuration = [r1, r2]

    Runnable startup = new RegistryStartup(logger, configuration)

    when:

    startup.run()

    then:

    1 * r1.run()

    then:

    1 * r2.run()

    then:

    configuration.empty
  }

  def "callback failure is logged and execution continues"() {
    Runnable r1 = Mock()
    Runnable r2 = Mock()
    Logger logger = Mock()
    RuntimeException ex = new RuntimeException("Crunch!")

    Runnable startup = new RegistryStartup(logger, [r1, r2])

    when:

    startup.run()

    then:

    1 * r1.run() >> { throw ex }
    1 * logger.error("An exception occurred during startup: {}", 'Crunch!', ex)
    1 * r2.run()
  }

  def "run may only be invoked once"() {
    Logger logger = Mock()
    Runnable startup = new RegistryStartup(logger, [])

    startup.run()

    when:

    startup.run()

    then:

    IllegalStateException e = thrown()

    e.message.contains "Method org.apache.tapestry5.ioc.internal.services.RegistryStartup.run"
    e.message.contains "may no longer be invoked."
  }

  def "integration test"() {
    when:

    def registry = new RegistryBuilder().add(StartupModule).build()

    then:

    !StartupModule.startupInvoked

    when:

    registry.performRegistryStartup()

    then:

    StartupModule.startupInvoked

    cleanup:

    registry.shutdown()

  }

}
