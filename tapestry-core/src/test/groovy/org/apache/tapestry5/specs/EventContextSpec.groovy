package org.apache.tapestry5.specs;

import java.time.LocalDate

import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.internal.EmptyEventContext
import org.apache.tapestry5.internal.URLEventContext
import org.apache.tapestry5.internal.services.ArrayEventContext
import org.apache.tapestry5.internal.services.ForceDevelopmentModeModule
import org.apache.tapestry5.ioc.annotations.ImportModule
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.modules.TapestryModule
import org.apache.tapestry5.services.ContextValueEncoder

import spock.lang.Specification

@ImportModule([TapestryModule, ForceDevelopmentModeModule ])
public class EventContextSpec extends Specification {

    @Inject
    TypeCoercer typeCoercer

    @Inject
    ContextValueEncoder contextValueEncoder

    def "ArrayEventContext toStrings()"() {

        when:
        def context = new ArrayEventContext(typeCoercer, input as Object[])

        then:
        context.toStrings() == expected

        where:
        input    | expected
        [1, 2.3] | ["1", "2.3"]
        []       | [] as String[]
    }

    def "EventContext toString()"() {

        when:
        def context = new ArrayEventContext(typeCoercer, 1, 2.3)

        then:
        context.toString() == "<EventContext: 1, 2.3>"
    }

    def "ArrayEventContext isEmpty()"() {

        when:
        def context = new ArrayEventContext(typeCoercer, input as Object[])

        then:
        context.isEmpty() == expected

        where:
        input    | expected
        [1, 2.3] | false
        []       | true
    }

    def "URLEventContext isEmpty()"() {

        when:
        def context = new URLEventContext(contextValueEncoder, input as String[])

        then:
        context.isEmpty() == expected

        where:
        input    | expected
        ["a", "b"] | false
        []       | true
    }

    def "EmptyEventContext isEmpty()"() {

        when:
        def context = new EmptyEventContext()

        then:
        context.isEmpty() == true
    }

    def "ArrayEventContext get() Exception"() {

        given:
        def context = new ArrayEventContext(typeCoercer, input as Object[])

        when:
        context.get(type, index)

        then:
        thrown(expectedException)

        where:
        input             | type      | index | expectedException
        [1, "2022-05-29"] | Integer   | 1     | RuntimeException                 // valid index, invalid type
        [1, "2022-05-29"] | String    | 2     | ArrayIndexOutOfBoundsException   // invalid index
    }

    def "ArrayEventContext tryGet()"() {

        given:
        def context = new ArrayEventContext(typeCoercer, input as Object[])

        when:
        def value = context.tryGet(type, index)

        then:
        value.isPresent() == isPresent

        where:
        input             | type      | index | isPresent
        [1, "String"]     | Integer   | 0     | true    // valid index and type
        [1, "2022-05-29"] | LocalDate | 1     | true    // valid index and type
        [1, "2022-05-29"] | String    | 1     | true    // valid index and coercable type
        [1, "2022-05-29"] | String    | 0     | true    // valid index and coercable type
        [1, "2022-05-29"] | Integer   | 1     | false   // valid index, invalid type
        [1, "2022-05-29"] | String    | 2     | false   // invalid index
    }
}
