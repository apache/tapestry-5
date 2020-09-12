package ioc.specs

import org.apache.tapestry5.beanmodel.internal.services.PropertyAccessImpl
import org.apache.tapestry5.commons.internal.util.TapestryException
import org.apache.tapestry5.commons.util.ExceptionUtils
import org.apache.tapestry5.ioc.test.util.ExceptionWrapper

import spock.lang.Shared
import spock.lang.Specification

class ExceptionUtilsSpec extends Specification {

    @Shared
    def access = new PropertyAccessImpl()

    def "find cause with match"() {
        when:
        def inner = new TapestryException("foo", null)
        def outer = new RuntimeException(inner)

        then:

        ExceptionUtils.findCause(outer, TapestryException).is(inner)
        ExceptionUtils.findCause(outer, TapestryException, access).is(inner)
    }

    def "find cause with no match"() {

        when:

        def re = new RuntimeException("No cause for you.")

        then:

        ExceptionUtils.findCause(re, TapestryException) == null
        ExceptionUtils.findCause(re, TapestryException, access) == null
    }

    def "find a hidden exception"() {
        when:

        def inner = new RuntimeException()
        def outer = new ExceptionWrapper(inner)

        then:

        // TAP5-1639: The old code can't find inner
        ExceptionUtils.findCause(outer, RuntimeException) == null

        // The new reflection-based on can:

        ExceptionUtils.findCause(outer, RuntimeException, access).is(inner)
    }

    def "toMessage(#exceptionToString) should be '#expected'"() {
        expect:

        ExceptionUtils.toMessage(ex) == expected

        where:

        ex                                               | expected
        new NullPointerException()                       | NullPointerException.name
        new IllegalArgumentException("Message provided") | "Message provided"

        exceptionToString = ex.toString()
    }
}
