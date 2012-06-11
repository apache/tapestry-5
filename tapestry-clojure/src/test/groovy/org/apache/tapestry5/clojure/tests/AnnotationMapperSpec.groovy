package org.apache.tapestry5.clojure.tests

import org.apache.tapestry5.clojure.FunctionName
import org.apache.tapestry5.internal.clojure.AnnotationMapper
import spock.lang.Specification
import spock.lang.Unroll

interface MyInterface {

  Object alpha()

  @FunctionName("beta-func")
  Object beta()

  @FunctionName("other/gamma-func")
  Object gamma()
}

class AnnotationMapperSpec extends Specification {

  def mapper = new AnnotationMapper()

  @Unroll
  def "Symbol for method #methodName should be #namespace / #name"() {


    def method = MyInterface.methods.find { it.name == methodName }

    when:

    def symbol = mapper.mapMethod("user", method)

    then:

    symbol.namespace == namespace
    symbol.name == name

    where:

    methodName | namespace | name
    "beta"     | "user"    | "beta-func"
    "gamma"    | "other"   | "gamma-func"
  }

  def "returns null if no annotation to provide a specific function name"() {

    when:

    def method = MyInterface.methods.find { it.name == "alpha" }

    then:

    mapper.mapMethod("user", method) == null

  }

}
