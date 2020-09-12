package ioc.specs

import org.apache.tapestry5.commons.internal.util.GenericsUtils
import org.apache.tapestry5.ioc.test.internal.util.NonGenericBean
import org.apache.tapestry5.ioc.test.internal.util.StringBean
import org.apache.tapestry5.ioc.test.internal.util.StringLongPair

import spock.lang.Specification
import spock.lang.Unroll

class GenericUtilsSpec extends Specification {

  def find(clazz, name) {
    def method = clazz.methods.find { it.name.equalsIgnoreCase(name) }

    if (method == null) {
      throw new IllegalArgumentException("Unable to find method '$name' of ${clazz.name}.")
    }

    return method
  }

  @Unroll
  def "generic return type for #method is #expected"() {

    expect:

    GenericsUtils.extractGenericReturnType(clazz, method).is(expected)

    where:

    clazz          | name       | expected
    NonGenericBean | "getvalue" | String
    StringBean     | "getvalue" | String
    StringLongPair | "getkey"   | String
    StringLongPair | "getvalue" | Long

    method = find(clazz, name)
  }

}
