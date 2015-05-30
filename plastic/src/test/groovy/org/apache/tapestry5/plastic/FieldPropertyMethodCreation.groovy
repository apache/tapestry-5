package org.apache.tapestry5.plastic

import spock.lang.Issue
import testannotations.Property
import testsubjects.AccessorsAlreadyExistSubject
import testsubjects.AccessorsAlreadyExistSubject2;
import testsubjects.CreateAccessorsSubject
import testsubjects.GenericCreateAccessorsSubject

import java.util.concurrent.atomic.AtomicReference

class FieldPropertyMethodCreation extends AbstractPlasticSpecification
{
    def withAccessors(Class subject, PropertyAccessType accessType) {
        def pc = mgr.getPlasticClass (subject.name)

        pc.getFieldsWithAnnotation(Property).each { f ->
            f.createAccessors(accessType)
        }

        def o = pc.createInstantiator().newInstance()
    }

    def "create accessors for fields"() {

        def o = withAccessors(CreateAccessorsSubject, PropertyAccessType.READ_WRITE)

        when:

        o.m_title = "via direct field access"

        then:

        assert o.m_title == o.title


        when:
        o.title = "via generated accessor"

        then: "Setting object property reflected in original field"

        assert o.m_title == o.title


        when:
        o.m_count = 1

        then: "Updates to primitive field reflected in generated getter"

        assert o.m_count == o.count

        when:
        o.count = 2

        then: "Setting primitive property reflected in original field"
        assert o.m_count == o.count
    }

    def "create accessors for generic fields"() {

        def o = withAccessors(GenericCreateAccessorsSubject, PropertyAccessType.READ_WRITE)

        def ref = new AtomicReference<String>("Plastic")

        o.ref = ref

        expect:

        assert o.ref == ref
        assert o.refValue == "Plastic"

        def get = o.class.getMethod("getRef")
        get.signature == "()Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;"
        get.genericInfo != null

        def set = o.class.getMethod("setRef", AtomicReference)
        set.signature == "(Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;)V"
        set.genericInfo != null
    }

    def "create getter that already exists"() {
        when:

        withAccessors(AccessorsAlreadyExistSubject, PropertyAccessType.READ_ONLY)

        then:

        def e = thrown(IllegalArgumentException)

        assert e.message == "Unable to create new accessor method public java.lang.String getValue() on class testsubjects.AccessorsAlreadyExistSubject as the method is already implemented."
    }

    @Issue('https://issues.apache.org/jira/browse/TAP5-2268')
    def "create getter that already exists with different return type"() {
      when:

      withAccessors(AccessorsAlreadyExistSubject2, PropertyAccessType.READ_ONLY)

      then:

      def e = thrown(IllegalArgumentException)

      assert e.message == "Unable to create new accessor method public int getValue() on class testsubjects.AccessorsAlreadyExistSubject2 as the method is already implemented."
    }

    def "create setter that already exists"() {
        when:

        withAccessors(AccessorsAlreadyExistSubject, PropertyAccessType.WRITE_ONLY)

        then:

        def e = thrown(IllegalArgumentException)

        assert e.message == "Unable to create new accessor method public void setValue(java.lang.String) on class testsubjects.AccessorsAlreadyExistSubject as the method is already implemented."
    }
}
