package ioc.specs

import org.apache.tapestry5.ioc.Invokable
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.services.ParallelExecutor
import org.apache.tapestry5.ioc.test.internal.services.NonParallelModule

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class NonParallelExecutorSpec extends Specification {

  @Shared
  @AutoCleanup("shutdown")
  private Registry registry

  @Shared
  private ParallelExecutor executor

  def setupSpec() {
    registry = new RegistryBuilder().add(NonParallelModule).build()

    executor = registry.getService ParallelExecutor
  }

  def "passing an Invokable will immediately invoke()"() {

    Invokable inv = Mock()

    when:

    def actual = executor.invoke(String, inv)

    then:

    actual == "value"

    1 * inv.invoke() >> "value"
  }

  def "A returned Future object is a simple wrapper around the result"() {
    Invokable inv = Mock()

    when:

    def future = executor.invoke(inv)

    then:

    1 * inv.invoke() >> "right now"

    !future.cancel(false)
    !future.cancelled
    future.done
    future.get() == "right now"
    future.get(0, null) == "right now"
  }
}