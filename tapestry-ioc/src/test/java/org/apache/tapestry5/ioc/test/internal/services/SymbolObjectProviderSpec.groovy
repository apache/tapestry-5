package org.apache.tapestry5.ioc.test.internal.services

import org.apache.tapestry5.commons.AnnotationProvider
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.ObjectProvider
import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.ioc.annotations.IntermediateType
import org.apache.tapestry5.ioc.annotations.Symbol
import org.apache.tapestry5.ioc.internal.services.SymbolObjectProvider
import org.apache.tapestry5.ioc.services.SymbolSource

import spock.lang.Specification

class SymbolObjectProviderSpec extends Specification {

  SymbolSource source = Mock()
  TypeCoercer coercer = Mock()
  AnnotationProvider annotationProvider = Mock()
  ObjectLocator locator = Mock()

  def "no @Symbol annotation present"() {
    ObjectProvider provider = new SymbolObjectProvider(source, coercer)

    when:

    def actual = provider.provide(Long, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Symbol) >> null

    actual == null
  }

  def "the symbol is expanded and coerced"() {
    Symbol annotation = Mock()
    def value = "my-symbol"
    def expanded = "123"
    def coerced = 123l

    ObjectProvider provider = new SymbolObjectProvider(source, coercer)

    when:

    def actual = provider.provide(Long, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Symbol) >> annotation
    1 * annotation.value() >> value
    1 * source.valueForSymbol(value) >> expanded
    1 * coercer.coerce(expanded, Long) >> coerced

    actual.is(coerced)
  }

  def "when @IntermediateType is present, coercion happens in two steps"() {
    Symbol annotation = Mock()
    IntermediateType it = Mock()
    def value = "my-symbol"
    def expanded = "123"
    def intermediateCoerced = new Date()
    def finalCoerced = 123l

    ObjectProvider provider = new SymbolObjectProvider(source, coercer)

    when:

    def actual = provider.provide(Long, annotationProvider, locator)

    then:

    1 * annotationProvider.getAnnotation(Symbol) >> annotation
    1 * annotationProvider.getAnnotation(IntermediateType) >> it
    1 * annotation.value() >> value
    1 * source.valueForSymbol(value) >> expanded
    1 * it.value() >> Date
    1 * coercer.coerce(expanded, Date) >> intermediateCoerced
    1 * coercer.coerce(intermediateCoerced, Long) >> finalCoerced

    actual.is(finalCoerced)

  }
}
