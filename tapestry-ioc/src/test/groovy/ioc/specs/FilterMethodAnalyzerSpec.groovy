package ioc.specs

import org.apache.tapestry5.ioc.internal.services.FilterMethodAnalyzer
import org.apache.tapestry5.ioc.internal.services.MethodSignature
import org.apache.tapestry5.ioc.test.internal.services.SampleFilter
import org.apache.tapestry5.ioc.test.internal.services.SampleService

import spock.lang.Specification
import spock.lang.Unroll

class FilterMethodAnalyzerSpec extends Specification {

  private MethodSignature find(clazz, name) {
    new MethodSignature(clazz.methods.find { it.name == name })
  }

  @Unroll
  def "position of delegate parameter for #methodName should be #position"() {
    def analyzer = new FilterMethodAnalyzer(SampleService)

    def mainMethod = find SampleService, methodName
    def filterMethod = find SampleFilter, methodName

    expect:

    analyzer.findServiceInterfacePosition(mainMethod, filterMethod) == position

    where:

    methodName                | position
    "simpleMatch"             | 0
    "mismatchParameterCount"  | -1
    "mismatchReturnType"      | -1
    "missingServiceInterface" | -1
    "complexMatch"            | 2

  }
}
