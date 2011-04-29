package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification;
import testannotations.InheritedAnnotation;
import testannotations.SimpleAnnotation;

class ClassAnnotationAccess extends Specification {
    def mgr = new PlasticManager()
    def pc = mgr.getPlasticClass("testsubjects.AnnotationSubject")

    def "access to non-existent annotation"() {

        expect:

        pc.hasAnnotation(Deprecated.class) == false
        pc.getAnnotation(Deprecated.class) == null
    }

    def "check existence of known, simple annotation"() {

        expect:
        pc.hasAnnotation(SimpleAnnotation.class) == true

        when:
        def a = pc.getAnnotation(SimpleAnnotation.class)

        then:
        a instanceof SimpleAnnotation

        a.annotationType() == SimpleAnnotation.class
        
        a.toString() == "@testannotations.SimpleAnnotation"
    }

    def "inherited class annotations visible in subclass"() {
        def pc = mgr.getPlasticClass("testsubjects.InheritedAnnotationSubClass")

        expect:
        pc.hasAnnotation(InheritedAnnotation.class) == true
    }
}
