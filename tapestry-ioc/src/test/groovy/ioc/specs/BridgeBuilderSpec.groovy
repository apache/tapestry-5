package ioc.specs

import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.ioc.internal.services.*
import org.apache.tapestry5.ioc.test.internal.services.ExtraFilterMethod
import org.apache.tapestry5.ioc.test.internal.services.ExtraServiceMethod
import org.apache.tapestry5.ioc.test.internal.services.MiddleFilter
import org.apache.tapestry5.ioc.test.internal.services.MiddleService
import org.apache.tapestry5.ioc.test.internal.services.StandardFilter
import org.apache.tapestry5.ioc.test.internal.services.StandardService
import org.apache.tapestry5.ioc.test.internal.services.ToStringFilter
import org.apache.tapestry5.ioc.test.internal.services.ToStringService
import org.slf4j.Logger

import spock.lang.Shared

class BridgeBuilderSpec extends AbstractSharedRegistrySpecification {

  @Shared
  PlasticProxyFactory proxyFactory;

  def setupSpec() {
    proxyFactory = getService PlasticProxyFactory
  }

  def "toString() of proxy is as expected"() {
    Logger logger = Mock()
    StandardFilter sf = Mock()
    StandardService ss = Mock()

    BridgeBuilder builder = new BridgeBuilder(logger, StandardService, StandardFilter, proxyFactory)

    when:

    def bridge = builder.instantiateBridge(ss, sf)

    then:

    bridge.toString() == "<PipelineBridge from org.apache.tapestry5.ioc.test.internal.services.StandardService to org.apache.tapestry5.ioc.test.internal.services.StandardFilter>"
  }

  def "standard service and interface"() {
    Logger logger = Mock()
    StandardFilter sf = Mock()
    StandardService ss = Mock()

    BridgeBuilder builder = new BridgeBuilder(logger, StandardService, StandardFilter, proxyFactory)
    def bridge = builder.instantiateBridge(ss, sf)

    when:

    assert bridge.run(5) == 18

    // 18 =  3 * (5 + 1)
    // so the filter runs first, and passes 6 to the service
    // seems there's an issue in Spock with chaining mocks this way

    then:

    1 * sf.run(_, _) >> { i, service -> service.run(i + 1) }

    1 * ss.run(_) >> { i -> 3 * i }

    0 * _
  }

  def "when toString() is part of service interface, it is forwarded through the filter"() {
    Logger logger = Mock()

    ToStringService service = new ToStringService() {

      String toString() { "Service" }
    }

    ToStringFilter filter = new ToStringFilter() {

      String toString(ToStringService s) {
        s.toString().toUpperCase()
      }
    }

    BridgeBuilder builder = new BridgeBuilder(logger, ToStringService, ToStringFilter, proxyFactory)

    when:

    ToStringService bridge = builder.instantiateBridge(service, filter)

    then:

    bridge.toString() == "SERVICE"
  }

  def "unmatched service interface method is logged and exception thrown"() {
    Logger logger = Mock()
    ExtraServiceMethod next = Mock()
    Serializable filter = Mock()

    BridgeBuilder builder = new BridgeBuilder(logger, ExtraServiceMethod, Serializable, proxyFactory)

    when:

    ExtraServiceMethod esm = builder.instantiateBridge(next, filter)

    then:

    1 * logger.error("Method void extraServiceMethod() has no match in filter interface java.io.Serializable.")

    when:

    esm.extraServiceMethod()

    then:

    RuntimeException e = thrown()

    e.message == "Method void extraServiceMethod() has no match in filter interface java.io.Serializable."
  }

  def "extra methods in filter interface are logged and ignored"() {
    Logger logger = Mock()
    Serializable next = Mock()
    ExtraFilterMethod filter = Mock()

    BridgeBuilder builder = new BridgeBuilder(logger, Serializable, ExtraFilterMethod, proxyFactory)

    when:

    assert builder.instantiateBridge(next, filter) != null

    then:

    1 * logger.error('Method {} of filter interface {} does not have a matching method in {}.', { it.name == "extraFilterMethod" }, ExtraFilterMethod.name, Serializable.name)

    0 * _
  }

  def "the service parameter may be a middle parameter of the filter method"() {
    Logger logger = Mock()

    MiddleFilter mf = new MiddleFilter() {

      @Override
      void execute(int count, char ch, MiddleService service, StringBuilder buffer) {
        service.execute(count, ch, buffer)

        buffer.append(' ')

        service.execute(count + 1, Character.toUpperCase(ch), buffer)
      }
    }

    MiddleService ms = new MiddleService() {

      @Override
      void execute(int count, char ch, StringBuilder buffer) {
        count.times() { buffer.append ch }
      }
    }

    BridgeBuilder builder = new BridgeBuilder(logger, MiddleService, MiddleFilter, proxyFactory)


    MiddleService bridge = builder.instantiateBridge(ms, mf)

    StringBuilder buffer = new StringBuilder("CODE: ")

    when:


    bridge.execute(3, 'a' as char, buffer)

    then:

    buffer.toString() == "CODE: aaa AAAA"

  }

}
