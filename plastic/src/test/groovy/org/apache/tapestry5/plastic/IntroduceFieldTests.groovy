package org.apache.tapestry5.plastic

import testsubjects.Empty

import java.lang.reflect.Modifier

class IntroduceFieldTests extends AbstractPlasticSpecification
{
    def "introduced fields are not visible in the allFields list"() {
        setup:
        def pc = mgr.getPlasticClass(Empty.name)

        expect:
        pc.allFields == []

        when:
        def f = pc.introduceField(String.name, "message")

        then:
        f.toString() == "PlasticField[private java.lang.String message (in class testsubjects.Empty)]"
        f.name == "message"
        pc.allFields == []
    }

    def "introducing a duplicate field name results in a unique id"() {
        setup:
        def pc = mgr.getPlasticClass(Empty.name)

        when:
        def f1 = pc.introduceField(Integer.name, "count")
        def f2 = pc.introduceField(Integer.name, "count")

        then:
        ! f1.is(f2)
        f1.name == "count"

        f2.name != "count"
        f2.name.startsWith("count")
    }


    def "instantiate a class with an introduced field"() {
        setup:
        def pc = mgr.getPlasticClass(Empty.name)

        pc.introduceField(Integer.name, "count")

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
