package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification
import testannotations.Maybe;

class FieldAnnotationAccess extends AbstractPlasticSpecification {

    def "locate field by annotation"() {
        setup:
        def pc  = mgr.getPlasticClass("testsubjects.AnnotationSubject")

        when:
        def fields = pc.getFieldsWithAnnotation(Maybe.class)

        then:
        fields.size() == 1
        fields[0].name == "hasMaybeAnnotation"
    }
}
