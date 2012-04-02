package org.apache.tapestry5.plastic

import org.apache.tapestry5.internal.plastic.NoopDelegate

import java.lang.reflect.InvocationTargetException

import testsubjects.*

class ObtainPlasticClass extends AbstractPlasticSpecification
{
    def "abstract classes may not be instantiated"() {
        def pc = mgr.getPlasticClass(AbstractSubject.name)

        when:

        pc.createInstantiator().newInstance()

        then:

        def e = thrown(IllegalStateException)

        assert e.message == "Class testsubjects.AbstractSubject is abstract and can not be instantiated."
    }

    def "access to simple empty class"() {
        setup:
        def pc = mgr.getPlasticClass(Empty.name)

        // More to come, but for now

        expect:
        pc != null

        pc.methods.empty == true
    }

    def "can obtain only method in class"() {
        setup:
        def pc = mgr.getPlasticClass(SingleMethod.name)
        def methods = pc.methods

        expect:
        methods.size() == 1
        methods.first().description.toString() == "public void aSingleMethod(int)"
    }

    def "static methods are ignored"() {
        setup:
        def pc = mgr.getPlasticClass(StaticMethodsIgnored.name)
        def methods = pc.methods

        expect:
        methods.size() == 1
        methods.first().description.toString() == "void anInstanceMethod()"
    }

    def "methods obtained in sorted alphabetic order"() {
        setup:
        def pc = mgr.getPlasticClass(MultipleMethods.name)
        def descs = pc.methods.collect({
            it.description.toString()
        });

        expect:

        descs == [
            "protected void barney(java.lang.String)",
            "private int betty(int, int, int)",
            "public java.lang.String betty(int, int)",
            "public void betty(int)",
            "public synchronized void betty()",
            "void fred()",
            "private void wilma() throws java.sql.SQLException"
        ]
    }

    def "fields obtained in sorted alphabetic order"() {
        setup:
        def pc = mgr.getPlasticClass(MultipleFields.name)

        expect:
        pc.fields.collect({ it.name }) == [
            "barney",
            "betty",
            "fred",
            "wilma"
        ]
    }

    def "static fields are ignored"() {
        setup:
        def pc = mgr.getPlasticClass(testsubjects.StaticFields.name)

        expect:
        pc.allFields == []
    }

    def "instrumented instance fields must be private"() {
        when:

        PlasticClass pc = mgr.getPlasticClass(testsubjects.PublicInstanceField.name)

        pc.getAllFields()[0].inject("fixed string value")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Field publicNotAllowed of class testsubjects.PublicInstanceField must be instrumented, and may not be public."
    }

    def "original constructors now throw exceptions"() {
        setup:

        def mgr = PlasticManager.withContextClassLoader().delegate(new NoopDelegate()).packages(["testsubjects"]).create()

        Class clazz = mgr.classLoader.loadClass(testsubjects.AlternateConstructor.name)

        when:

        clazz.getConstructor([String] as Class[]).newInstance([null]as Object[])

        then:

        def e = thrown(InvocationTargetException)

        e.cause.class == IllegalStateException

        e.cause.message == "Class testsubjects.AlternateConstructor has been transformed and may not be directly instantiated."

        when:

        clazz.getConstructor([]as Class[]).newInstance([]as Object[])

        then:

        e = thrown(InvocationTargetException)

        e.cause.class == IllegalStateException

        e.cause.message == "Class testsubjects.AlternateConstructor has been transformed and may not be directly instantiated."
    }
}