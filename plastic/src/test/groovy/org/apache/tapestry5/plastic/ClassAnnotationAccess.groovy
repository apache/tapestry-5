package org.apache.tapestry5.plastic

import testannotations.InheritedAnnotation
import testannotations.SimpleAnnotation
import testsubjects.AnnotationSubject
import testsubjects.InheritedAnnotationSubClass

class ClassAnnotationAccess extends AbstractPlasticSpecification {

    def pc = mgr.getPlasticClass(AnnotationSubject.name)

    def "access to non-existent annotation"() {

        expect:

        pc.hasAnnotation(Deprecated) == false
        pc.getAnnotation(Deprecated) == null
    }

    def "check existence of known, simple annotation"() {

        expect:
        pc.hasAnnotation(SimpleAnnotation) == true

        when:
        def a = pc.getAnnotation(SimpleAnnotation)

        then:
        a instanceof SimpleAnnotation

        a.annotationType() == SimpleAnnotation

        a.toString() == "@testannotations.SimpleAnnotation"
    }

    def "inherited class annotations visible in subclass"() {
        def pc = mgr.getPlasticClass(InheritedAnnotationSubClass.name)

        expect:
        pc.hasAnnotation(InheritedAnnotation) == true
    }
}
