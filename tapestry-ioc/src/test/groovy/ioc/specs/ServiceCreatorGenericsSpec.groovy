package ioc.specs

import static org.apache.tapestry5.ioc.internal.AbstractServiceCreator.findParameterizedTypeFromGenericType

import java.lang.reflect.Method

import org.apache.tapestry5.ioc.internal.AbstractServiceCreator
import org.apache.tapestry5.ioc.internal.IOCMessages
import org.apache.tapestry5.ioc.test.internal.ServiceBuilderMethodFixture

import spock.lang.Specification

class ServiceCreatorGenericsSpec extends Specification {

  Method findMethod(name) {
    Method method = ServiceBuilderMethodFixture.methods.find { it.name == name}

    assert method != null

    return method
  }

  def methodMissing(String name, args) {
    AbstractServiceCreator."$name"(* args)
  }

  def "parameterized type of generic method parameter is extracted"() {

    when:

    def method = findMethod "methodWithParameterizedList"

    then:

    method.parameterTypes[0] == List

    def type = method.genericParameterTypes[0]

    type.toString() == "java.util.List<java.lang.Runnable>"

    findParameterizedTypeFromGenericType(type) == Runnable
  }

  def "parameterized type of a non-generic parameter is Object"() {

    when:

    def method = findMethod "methodWithList"

    then:

    method.parameterTypes[0] == List

    def type = method.genericParameterTypes[0]

    type.toString() == "interface java.util.List"
    findParameterizedTypeFromGenericType(type) == Object
  }

  def "getting parameterized type for a non-support type is a failure"() {

    when:

    def method = findMethod "methodWithWildcardList"

    then:

    method.parameterTypes[0] == List

    def type = method.genericParameterTypes[0]

    when:

    findParameterizedTypeFromGenericType(type)

    then:

    IllegalArgumentException e = thrown()

    e.message == IOCMessages.genericTypeNotSupported(type)
  }

}
