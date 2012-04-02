package org.apache.tapestry5.plastic

import testsubjects.MethodHandleSubject
import testsubjects.WillNotDoubleException

class MethodHandleAccess extends AbstractPlasticSpecification {

    def init(name) {
        def pc = mgr.getPlasticClass(MethodHandleSubject.name)

        def handle = findHandle(pc, name)

        return [
            pc.createInstantiator().newInstance(),
            handle
        ]
    }

    def "void method with primitive parameters"() {

        def (instance, handle) = init("voidPrimitiveParameters")

        when:

        def result = handle.invoke(instance, true, Long.MAX_VALUE, 1234)

        then:

        result.returnValue == null
        result.didThrowCheckedException() == false

        instance.message == "bool: true, long: 9223372036854775807, int: 1234"
    }

    def "method that may throw a checked exception"() {

        def (instance, handle) = init("mayThrowException")

        when:

        def result = handle.invoke(instance, 5, false)

        then:

        result.returnValue == 10
        result.didThrowCheckedException() == false

        when:

        result = handle.invoke(instance, 7, true)

        then:

        result.returnValue == null
        result.didThrowCheckedException() == true

        def e = result.getCheckedException(WillNotDoubleException)
    }

    // This also tests object references as parameters and return values (other tests
    // covered primitives).
    def "access to a private method that returns an object"() {

        def (instance, handle) = init("wrapString")

        when:

        // Ugly because Groovy does not have a character literal syntax

        def result = handle.invoke(instance, "plastic", '['.charAt(0), ']'.charAt(0))

        then:

        result.returnValue == "[plastic]"
    }

    def "access to a private method that returns void"() {
        def (instance, handle) = init("forceMessage")

        def message = 'Updated'

        when:

        def result = handle.invoke(instance, message)

        then:

        assert instance.message.is(message)
    }
}
