package org.apache.tapestry5.internal.plastic

import spock.lang.Specification
import testsubjects.ContextCatcher

import java.text.DateFormat

class ClassInstantiatorTests extends Specification
{

    def ins = new ClassInstantiatorImpl(ContextCatcher, ContextCatcher.constructors[0], null)

    def "adding a context value returns a new instantiator"() {

        String value = "instance value of type String";

        when:
        def ins2 = ins.with(String, value)

        then:
        ! ins2.is(ins)

        ins2.get(String).is(value)
    }

    def "may not add a duplicate instance context value"() {

        given:
        def ins2 = ins.with(String, "initial value")

        when:
        ins2.with(String, "conflicting value")

        then:
        def e = thrown(IllegalStateException)

        e.message == "An instance context value of type java.lang.String has already been added."
    }

    def "get a value not stored is a failure"() {
        when:
        ins.get(DateFormat)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Instance context for class testsubjects.ContextCatcher does not contain a value for type class java.text.DateFormat."
    }

    def "instance map wrapped as InstanceContext and passed to constructed object"() {
        def value = "instance value of type String"

        when:

        def o = ins.with(String, value).newInstance()

        then:

        o.instanceContext.get(String).is(value)
    }
}
