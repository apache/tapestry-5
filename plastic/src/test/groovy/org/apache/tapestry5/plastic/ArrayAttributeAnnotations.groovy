package org.apache.tapestry5.plastic

import testannotations.ArrayAnnotation
import testannotations.Truth
import testsubjects.AnnotationSubject
import testsubjects.ArrayAttributesSubject
import testsubjects.ExplicityEmptyArrayAttributesSubject

class ArrayAttributeAnnotations extends AbstractPlasticSpecification {

    def "handling of array attribute defaults"() {
        when:
        def pc = mgr.getPlasticClass(AnnotationSubject.name)

        def a = pc.getAnnotation(ArrayAnnotation)

        then:
        a.numbers().length == 0
        a.strings().length == 0
        a.types().length == 0
        a.annotations().length == 0
    }

    def "explicit values for array attributes"() {
        when:
        def pc = mgr.getPlasticClass(ArrayAttributesSubject.name)
        def a = pc.getAnnotation(ArrayAnnotation)

        then:

        a.numbers() == [5]

        a.strings() == ["frodo", "sam"]

        a.types() == [Runnable]

        a.annotations().length == 2
        a.annotations()[0].value() == Truth.YES
        a.annotations()[1].value() == Truth.NO
    }

    def "handling of explicitly empty array attributes"() {
        when:
        def pc = mgr.getPlasticClass(ExplicityEmptyArrayAttributesSubject.name)

        def a = pc.getAnnotation(ArrayAnnotation)

        then:
        a.numbers().length == 0
        a.strings().length == 0
        a.types().length == 0
        a.annotations().length == 0
    }
}
