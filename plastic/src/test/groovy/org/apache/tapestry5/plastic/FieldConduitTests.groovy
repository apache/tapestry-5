// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.plastic

class FieldConduitTests extends AbstractPlasticSpecification
{
    def "setting a field invokes the conduit"() {

        FieldConduit fc = Mock()

        def pc = mgr.getPlasticClass("testsubjects.IntFieldHolder")

        pc.allFields.first().setConduit(fc)

        def o = pc.createInstantiator().newInstance()

        when:

        o.setValue(123)

        then:

        1 * fc.set(_, _, 123)

        when:
        fc.get(_, _) >>> 999

        then:

        o.getValue() == 999

        expect:

        // The field doesn't really get used (it may be removed some day, though its useful because of the
        // annotations that may be on it).

        o.@value == 0
    }

    def "use of computed conduit"() {
        FieldConduit fc = Mock()

        def pc = mgr.getPlasticClass("testsubjects.IntFieldHolder")

        pc.allFields.first().setComputedConduit({ return fc } as ComputedValue)

        def o = pc.createInstantiator().newInstance()

        when:

        o.setValue(456)

        then:

        1 * fc.set(_, _, 456)
    }

    def "field initializations are visible to the conduit"() {
        FieldConduit fc = Mock()

        def pc = mgr.getPlasticClass("testsubjects.LongFieldHolder")

        pc.allFields.first().setConduit(fc)

        when:

        pc.createInstantiator().newInstance()

        then:

        // 100 is the initial value of the field

        1 * fc.set(_, _, 100)
    }

    /**
     * When an inner class accesses private members of its containing class, the compiler generates
     * synthetic static methods (package private visibility).  This ensures that those methods are
     * also subject to field access transformations.
     */
    def "inner class access methods are routed through field conduit"() {

        FieldConduit fc = Mock()

        def mgr = new PlasticManager (Thread.currentThread().contextClassLoader, [
                    transform: { PlasticClass pc ->
                        pc.allFields.first().setConduit(fc)
                    },
                    configureInstantiator: { className, instantiator -> instantiator }
                ] as PlasticManagerDelegate , ["testsubjects"]as Set)


        def o = mgr.getClassInstantiator("testsubjects.AccessMethodsSubject").newInstance()

        def i = o.valueAccess

        when:

        i.set("funky")

        then:

        1 * fc.set(o, _, "funky")

        when:

        assert i.get() == "plastic"

        then:

        1 * fc.get(o, _) >> "plastic"
    }
}
