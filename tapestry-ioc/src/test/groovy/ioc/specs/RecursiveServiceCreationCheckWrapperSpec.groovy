package ioc.specs

import org.apache.tapestry5.commons.ObjectCreator
import org.apache.tapestry5.ioc.def.ServiceDef
import org.apache.tapestry5.ioc.internal.ObjectCreatorSource
import org.apache.tapestry5.ioc.internal.RecursiveServiceCreationCheckWrapper
import org.apache.tapestry5.ioc.internal.ServiceDefImpl
import org.slf4j.Logger

class RecursiveServiceCreationCheckWrapperSpec extends AbstractSharedRegistrySpecification {

  static DESCRIPTION = "{SOURCE DESCRIPTION}"

  Logger logger = Mock()
  ObjectCreatorSource source = Mock()
  ObjectCreator delegate = Mock()
  Object service = Mock()

  ServiceDef sd = new ServiceDefImpl(Runnable, null, "Bar", null, "singleton", false, false, source)

  def "ensure that the creator is called only once"() {

    when:

    ObjectCreator wrapper = new RecursiveServiceCreationCheckWrapper(sd, delegate, logger)

    def actual = wrapper.createObject()

    then:

    actual == service

    1 * delegate.createObject() >> service

    when:

    wrapper.createObject()

    then:

    IllegalStateException e = thrown()

    e.message.contains "Construction of service 'Bar' has failed due to recursion"
    e.message.contains DESCRIPTION

    1 * source.description >> DESCRIPTION
  }

  def "construction exceptions are logged properly"() {

    def t = new RuntimeException("Just cranky.")

    when:

    ObjectCreator wrapper = new RecursiveServiceCreationCheckWrapper(sd, delegate, logger)

    wrapper.createObject()

    then:

    RuntimeException e = thrown()

    e.is(t)

    1 * delegate.createObject() >> { throw t }

    1 * logger.error("Construction of service Bar failed: ${t.message}", t)


    when: "a subsequent call"

    def actual = wrapper.createObject()

    then: "the delegate is reinvoked (succesfully, this time)"

    actual.is(service)

    1 * delegate.createObject() >> service
  }


}
