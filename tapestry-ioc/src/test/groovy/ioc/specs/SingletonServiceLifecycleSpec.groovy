package ioc.specs

import org.apache.tapestry5.commons.ObjectCreator
import org.apache.tapestry5.ioc.ServiceResources
import org.apache.tapestry5.ioc.internal.SingletonServiceLifecycle

import spock.lang.Specification

class SingletonServiceLifecycleSpec extends Specification {

  def "creator is invoked"() {
    ServiceResources resources = Mock()
    ObjectCreator creator = Mock()
    def expected = new Object()

    def lifecycle = new SingletonServiceLifecycle()

    when:

    def actual = lifecycle.createService(resources, creator)

    then:

    1 * creator.createObject() >> expected

    actual.is expected
  }
}
