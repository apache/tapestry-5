package org.apache.tapestry5.clojure.tests

import org.apache.tapestry5.internal.clojure.DefaultMapper
import spock.lang.Specification
import spock.lang.Unroll

class DefaultFunctionNameMapperSpec extends Specification {

  DefaultMapper mapper = new DefaultMapper()

  @Unroll
  def "method name '#methodName' should map to function name '#functionName'"() {

    expect:

    mapper.transformName(methodName) == functionName

    where:

    methodName        | functionName
    "simple"          | "simple"
    "caseChangePoint" | "case-change-point"
    "toASCII"         | "to-aSCII"         // Questionable!
  }

  def "default conversion right from method"() {

    def method = Object.methods.find { it.name == "equals" }

    when:

    def symbol = mapper.mapMethod("user", method)

    then:

    symbol.namespace == "user"
    symbol.name == "equals"
  }
}
