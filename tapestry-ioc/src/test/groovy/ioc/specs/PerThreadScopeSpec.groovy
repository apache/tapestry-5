package ioc.specs

import org.apache.tapestry5.ioc.test.PerThreadModule
import org.apache.tapestry5.ioc.test.ScopeMismatchModule
import org.apache.tapestry5.ioc.test.StringHolder

class PerThreadScopeSpec extends AbstractRegistrySpecification {

  def "ensure that different threads see different implementations"() {
    def threadExecuted = false

    buildRegistry PerThreadModule

    def holder = getService StringHolder

    when:

    holder.value = "fred"

    then:

    holder.value == "fred"

    when:

    Thread t = new Thread({
      assert holder.value == null

      holder.value = "barney"

      assert holder.value == "barney"

      threadExecuted = true

      cleanupThread()
    })

    t.start()
    t.join()

    then:

    threadExecuted
    holder.value == "fred"
  }

  def "services with out a service interface must use the default scope"() {

    buildRegistry ScopeMismatchModule

    when:

    getService StringBuilder

    then:

    Exception e = thrown()

    e.message.contains "Error building service proxy for service 'ScopeRequiresAProxyAndNoInterfaceIsProvided'"
    e.message.contains "Service scope 'perthread' requires a proxy"
  }

  def "ensure that perthread services are discarded by cleanupThread()"() {
    buildRegistry PerThreadModule

    when:

    def holder = getService StringHolder

    then:

    holder.value == null

    when:

    holder.value = "fred"

    then:

    holder.value == "fred"

    when:

    cleanupThread()

    then:

    holder.value == null

  }
}
