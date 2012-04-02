package org.apache.tapestry5.plastic

import java.lang.reflect.Modifier

class IntroduceFieldTests extends AbstractPlasticSpecification
{
    def "introduced fields are not visible in the allFields list"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.Empty")

        expect:
        pc.allFields == []

        when:
        def f = pc.introduceField("java.lang.String", "message")

        then:
        f.toString() == "PlasticField[private java.lang.String message (in class testsubjects.Empty)]"
        f.name == "message"
        pc.allFields == []
    }

    def "introducing a duplicate field name results in a unique id"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.Empty")

        when:
        def f1 = pc.introduceField("java.lang.Integer", "count")
        def f2 = pc.introduceField("java.lang.Integer", "count")

        then:
        ! f1.is(f2)
        f1.name == "count"

        f2.name != "count"
        f2.name.startsWith("count")
    }


    def "instantiate a class with an introduced field"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.Empty")

        pc.introduceField("java.lang.Integer", "count")

        def ins = pc.createInstantiator()

        when:
        def empty = ins.newInstance()
        def f = empty.class.getDeclaredField("count")

        // Use Groovy to access the field directly

        empty.@count = 77

        then:
        empty.@count == 77

        when:
        empty.@count = 99

        then:
        empty.@count == 99

        expect:

        f.modifiers == Modifier.PRIVATE
        f.type == Integer
    }
}
