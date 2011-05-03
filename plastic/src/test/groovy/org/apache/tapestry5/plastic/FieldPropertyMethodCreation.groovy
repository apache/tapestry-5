// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.plastic

import java.util.concurrent.atomic.AtomicReference

import spock.lang.Specification
import testannotations.Property
import testsubjects.AccessorsAlreadyExistSubject;
import testsubjects.CreateAccessorsSubject
import testsubjects.GenericCreateAccessorsSubject

class FieldPropertyMethodCreation extends AbstractPlasticSpecification
{
    def withAccessors(Class subject, PropertyAccessType accessType) {
        def pc = mgr.getPlasticClass (subject.name)

        pc.getFieldsWithAnnotation(Property.class).each { f ->
            f.createAccessors(accessType)
        }

        def o = pc.createInstantiator().newInstance()
    }

    def "create accessors for fields"() {

        def o = withAccessors(CreateAccessorsSubject.class, PropertyAccessType.READ_WRITE)

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

        def o = withAccessors(GenericCreateAccessorsSubject.class, PropertyAccessType.READ_WRITE)

        def ref = new AtomicReference<String>("Plastic")

        o.ref = ref

        expect:

        assert o.ref == ref
        assert o.refValue == "Plastic"

        o.class.getMethod("getRef").signature == "()Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;"
        o.class.getMethod("setRef", AtomicReference.class).signature == "Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;"
    }

    def "create getter that already exists"() {
        when:

        withAccessors(AccessorsAlreadyExistSubject.class, PropertyAccessType.READ_ONLY)

        then:

        def e = thrown(IllegalArgumentException)

        assert e.message == "Unable to create new accessor method public java.lang.String getValue() on class testsubjects.AccessorsAlreadyExistSubject as the method is already implemented."
    }

    def "create setter that already exists"() {
        when:

        withAccessors(AccessorsAlreadyExistSubject.class, PropertyAccessType.WRITE_ONLY)

        then:

        def e = thrown(IllegalArgumentException)

        assert e.message == "Unable to create new accessor method public void setValue(java.lang.String) on class testsubjects.AccessorsAlreadyExistSubject as the method is already implemented."
    }
}
