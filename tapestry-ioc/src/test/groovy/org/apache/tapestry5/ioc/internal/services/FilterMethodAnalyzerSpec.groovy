package org.apache.tapestry5.ioc.internal.services

import spock.lang.Specification
import spock.lang.Unroll


class FilterMethodAnalyzerSpec extends Specification {

  def find(clazz, name) {
    new MethodSignature(clazz.methods.find { it.name == name })
  }

  @Unroll
  def "position of delegate parameter for #filterMethod should be #position"() {
    def analyzer = new FilterMethodAnalyzer(SampleService)

    expect:

    analyzer.findServiceInterfacePosition(mainMethod, filterMethod) == position

    where:

    methodName                | position
    "simpleMatch"             | 0
    "mismatchParameterCount"  | -1
    "mismatchReturnType"      | -1
    "missingServiceInterface" | -1
    "complexMatch"            | 2

    mainMethod = find SampleService, methodName
    filterMethod = find SampleFilter, methodName
  }
}
