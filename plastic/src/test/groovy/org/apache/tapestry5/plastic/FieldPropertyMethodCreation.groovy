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

import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PropertyAccessType;

import spock.lang.Specification;
import testannotations.Property;

class FieldPropertyMethodCreation extends Specification
{
    def "create accessors for fields"() {
        setup:

        def mgr = new PlasticManager()
        def pc = mgr.getPlasticClass ("testsubjects.CreateAccessorsSubject")

        pc.getFieldsWithAnnotation(Property.class).each { f -> f.createAccessors(PropertyAccessType.READ_WRITE) }

        def o = pc.createInstantiator().newInstance()

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
}
