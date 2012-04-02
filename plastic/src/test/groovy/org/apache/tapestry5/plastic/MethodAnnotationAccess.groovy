package org.apache.tapestry5.plastic

import testannotations.Maybe
import testannotations.Outer
import testannotations.PrimitiveValues
import testannotations.Truth
import testsubjects.AnnotationSubject

class MethodAnnotationAccess extends AbstractPlasticSpecification {

    def pc = mgr.getPlasticClass(AnnotationSubject.name)

    def "access to method annotation with enum attribute"() {

        when:

        def methods = pc.getMethodsWithAnnotation(Maybe)
        def noMethod = methods[0]
        def yesMethod = methods[1]

        then:

        methods.size == 2

        noMethod.description.methodName == "no"
        yesMethod.description.methodName == "yes"

        noMethod.getAnnotation(Maybe).value() == Truth.NO
        yesMethod.getAnnotation(Maybe).value() == Truth.YES
    }

    def "method annotation with primitive attributes"() {
        when:
        def methods = pc.getMethodsWithAnnotation(PrimitiveValues)

        then:
        methods.size == 1

        when:
        def pv = methods[0].getAnnotation(PrimitiveValues)

        then:
        pv.count()  == 5
        pv.title() == "runnables"  // explicit
        pv.type() == Runnable
        pv.message() == "created" // default
    }

    def "nested annotation as attribute of outer annotation"() {
        when:
        def ann = pc.getMethodsWithAnnotation(Outer)[0].getAnnotation(Outer)

        then:
        ann.maybe().value() == Truth.YES
    }
}
