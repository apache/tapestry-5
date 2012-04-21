package org.apache.tapestry5.ioc.internal.services

import org.apache.tapestry5.ioc.services.ClassNameLocator
import spock.lang.Specification

class ClassNameLocatorImplSpec extends Specification {

  ClassNameLocator locator = new ClassNameLocatorImpl(new ClasspathURLConverterImpl());

  def assertInList(classNames, packageName, String... expectedNames) {

    expectedNames.each { name ->
      String qualifiedName = "${packageName}.${name}"

      assert classNames.contains(qualifiedName), "[$qualifiedName] not present in ${classNames.join(', ')}."
    }
  }

  def assertNotInList(classNames, packageName, String... expectedNames) {

    expectedNames.each { name ->
      String qualifiedName = "${packageName}.${name}"

      assert !classNames.contains(qualifiedName), "[$qualifiedName] should not be present in ${classNames.join(', ')}."
    }
  }

  def "locate classes inside a JAR file on the classpath"() {

    expect:

    assertInList locator.locateClassNames("javax.inject"),
        "javax.inject",
        "Inject", "Named", "Singleton"
  }

  def "can locate classes inside a subpackage, inside a classpath JAR file"() {

    expect:

    assertInList locator.locateClassNames("org.slf4j"),
        "org.slf4j",
        "spi.MDCAdapter"
  }

  def "can locate classes in local folder, but exclude inner classes"() {

    def packageName = "org.apache.tapestry5.ioc.services"

    when:

    def names = locator.locateClassNames packageName

    then:

    assertInList names, packageName, "SymbolSource", "TapestryIOCModule"

    assertNotInList names, packageName, 'TapestryIOCMOdules$1'
  }

  def "can locate classes in subpackage of local folders"() {
    def packageName = "org.apache.tapestry5"

    when:

    def names = locator.locateClassNames packageName

    then:

    assertInList names, packageName, "ioc.Orderable", "ioc.services.ChainBuilder"
    assertNotInList names, packageName, 'services.TapestryIOCModule$1'
  }

}
