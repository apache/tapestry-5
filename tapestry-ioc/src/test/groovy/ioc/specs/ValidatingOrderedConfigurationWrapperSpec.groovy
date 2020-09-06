package ioc.specs

import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.OrderedConfiguration
import org.apache.tapestry5.ioc.internal.TypeCoercerProxy
import org.apache.tapestry5.ioc.internal.ValidatingOrderedConfigurationWrapper
import org.apache.tapestry5.ioc.internal.util.Orderer
import org.slf4j.Logger

import spock.lang.Specification

class ValidatingOrderedConfigurationWrapperSpec extends Specification {

  def "contribution of a coerceable instance"() {
    Runnable contribution = Mock()
    Runnable coerced = Mock()
    Runnable pre = Mock()
    Runnable post = Mock()
    Logger logger = Mock()
    TypeCoercerProxy tc = Mock()

    def orderer = new Orderer(logger)

    orderer.add "pre", pre
    orderer.add "post", post

    OrderedConfiguration config = new ValidatingOrderedConfigurationWrapper(Runnable, null, tc, orderer, null, null)

    when:

    config.add("id", contribution, "after:pre", "before:post")

    then:

    1 * tc.coerce(contribution, Runnable) >> coerced

    orderer.ordered == [pre, coerced, post]
  }

  def "contribution of a valid type"() {
    Map instance = new HashMap()
    Map pre = Mock()
    Map post = Mock()
    ObjectLocator locator = Mock()
    TypeCoercerProxy tc = Mock()
    Logger logger = Mock()

    def orderer = new Orderer(logger)

    orderer.add "pre", pre
    orderer.add "post", post

    OrderedConfiguration config = new ValidatingOrderedConfigurationWrapper(Map, locator, tc, orderer, null, null)

    when:

    config.addInstance("id", HashMap, "after:pre", "before:post")

    then:

    1 * locator.autobuild(HashMap) >> instance
    1 * tc.coerce(instance, Map) >> instance

    orderer.ordered == [pre, instance, post]
  }

  def "null objected passed through"() {
    Logger logger = Mock()

    Orderer orderer = new Orderer(logger)
    OrderedConfiguration config = new ValidatingOrderedConfigurationWrapper(Runnable, null, null, orderer, null, null)

    when:

    config.add("id", null)

    then:

    orderer.ordered.empty


  }
}
