package ioc.specs

import org.apache.tapestry5.ioc.internal.services.ClassNameLocatorImpl
import org.apache.tapestry5.ioc.internal.services.ClasspathScannerImpl
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl
import org.apache.tapestry5.ioc.services.ClassNameLocator
import org.apache.tapestry5.ioc.services.ClasspathScanner
import spock.lang.Specification

class ClassNameLocatorImplSpec extends Specification {

    ClasspathScanner scanner = new ClasspathScannerImpl(new ClasspathURLConverterImpl())

    ClassNameLocator locator = new ClassNameLocatorImpl(scanner);

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

        def packageName = "org.apache.tapestry5.commons.services"

        when:

        def names = locator.locateClassNames packageName

        then:

        assertInList names, packageName, "DataTypeAnalyzer", "CoercionTuple"

        // This is an inner class and those should never be provided.
        assertNotInList names, packageName, 'CoercionTuple$CoercionWrapper'
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
