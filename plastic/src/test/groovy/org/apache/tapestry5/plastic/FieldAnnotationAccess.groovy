package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification
import testannotations.Maybe;

class FieldAnnotationAccess extends Specification {
    def mgr = new PlasticManager()

    def "locate field by annotation"() {
        setup:
        def pc  = mgr.getPlasticClass("testsubjects.AnnotationSubject")

        when:
        def fields = pc.getFieldsWithAnnotation(Maybe.class)

        then:
        fields.size() == 1
        fields[0].name == "hasMaybeAnnotation"
    }

    def "claimed fields not visible to getFieldsWithAnnotation()"() {
        setup:
        def pc  = mgr.getPlasticClass("testsubjects.AnnotationSubject")

        when:
        def fields = pc.getFieldsWithAnnotation(Maybe.class)

        fields[0].claim(this)

        then:

        pc.getFieldsWithAnnotation(Maybe.class).empty
    }
}
