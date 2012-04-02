package org.apache.tapestry5.plastic

import testannotations.Maybe

class FieldAnnotationAccess extends AbstractPlasticSpecification
{

    def "read static field"()
    {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.AnnotationSubject")

        when:
        def fields = pc.getFieldsWithAnnotation(Maybe)

        then:
        fields.size() == 1
        fields[0].name == "hasMaybeAnnotation"
    }
}
