package ioc.specs

import org.apache.tapestry5.commons.Configuration
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.ioc.internal.TypeCoercerProxy
import org.apache.tapestry5.ioc.internal.ValidatingConfigurationWrapper

import spock.lang.Specification

class ValidatingConfigurationWrapperSpec extends Specification {

  TypeCoercerProxy tc = Mock()
  ObjectLocator locator = Mock()

  def collection = []

  def "valid contribution"() {
    Runnable value = Mock()

    Configuration config = new ValidatingConfigurationWrapper(Runnable, null, tc, collection, "Baz")

    when:

    config.add(value)

    then:

    tc.coerce(value, Runnable) >> value

    collection == [value]
  }

  def "contributed value may be coerced"() {
    Runnable value = Mock()
    Runnable coerced = Mock()

    Configuration config = new ValidatingConfigurationWrapper(Runnable, null, tc, collection, "Baz")

    when:

    config.add(value)

    then:

    tc.coerce(value, Runnable) >> coerced

    collection == [coerced]
  }

  def "an instance of a class may be contributed"() {
    HashMap contributed = new HashMap()
    Map coerced = Mock()

    Configuration config = new ValidatingConfigurationWrapper(Map, locator, tc, collection, "Baz")

    when:

    config.addInstance(HashMap)

    then:

    locator.autobuild(HashMap) >> contributed
    tc.coerce(contributed, Map) >> coerced

    collection == [coerced]
  }

  def "null may not be contributed"() {
    Configuration config = new ValidatingConfigurationWrapper(Runnable, null, tc, collection, "Baz")

    when:

    config.add(null)

    then:

    NullPointerException e = thrown()

    e.message == "Service contribution (to service 'Baz') was null."
  }
}
