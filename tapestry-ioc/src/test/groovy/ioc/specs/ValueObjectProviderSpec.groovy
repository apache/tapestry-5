package ioc.specs

import org.apache.tapestry5.commons.AnnotationProvider
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.ObjectProvider
import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.ioc.annotations.IntermediateType
import org.apache.tapestry5.ioc.annotations.Value
import org.apache.tapestry5.ioc.internal.services.ValueObjectProvider
import org.apache.tapestry5.ioc.services.SymbolSource

import spock.lang.Specification

class ValueObjectProviderSpec extends Specification {

  SymbolSource symbolSource = Mock()
  TypeCoercer coercer = Mock()
  AnnotationProvider annotationProvider = Mock()
  ObjectLocator locator = Mock()

  ObjectProvider provider = new ValueObjectProvider(symbolSource, coercer)

  def "@Value annotation not present"() {

    when:

    def value = provider.provide(Runnable, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Value) >> null

    value == null
  }

  def "symbols in @Value annotation are expanded, then coerced"() {
    def annotationValue = '${foo}'
    def expanded = 'Foo'
    Runnable coerced = Mock()
    Value value = Mock()

    when:

    def result = provider.provide(Runnable, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Value) >> value
    1 * annotationProvider.getAnnotation(IntermediateType) >> null
    1 * value.value() >> annotationValue
    1 * symbolSource.expandSymbols(annotationValue) >> expanded
    1 * coercer.coerce(expanded, Runnable) >> coerced

    result.is coerced
  }

  def "if @IntermediateType is present, then coercion occurs in two steps"() {
    def annotationValue = '${foo}'
    def expanded = 'Foo'
    BigDecimal intermediate = new BigDecimal("1234")
    Runnable coerced = Mock()
    Value value = Mock()
    IntermediateType intermediateType = Mock()

    when:

    def result = provider.provide(Runnable, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Value) >> value
    1 * annotationProvider.getAnnotation(IntermediateType) >> intermediateType
    1 * intermediateType.value() >> BigDecimal
    1 * value.value() >> annotationValue
    1 * symbolSource.expandSymbols(annotationValue) >> expanded
    1 * coercer.coerce(expanded, BigDecimal) >> intermediate
    1 * coercer.coerce(intermediate, Runnable) >> coerced

    result.is coerced
  }
}
