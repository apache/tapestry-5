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

import testsubjects.*

class FieldHandleTests extends AbstractPlasticSpecification
{
    def "getting access to a new primitive field"() {
        def pc = mgr.getPlasticClass(Empty.name)

        def f = pc.introduceField("int", "count")
        def h = f.handle

        def ins = pc.createInstantiator()

        when:
        def empty = ins.newInstance()

        // Use Groovy to access the field directly

        empty.@count = 77

        then:
        h.get(empty) == 77

        when:
        h.set(empty, 99)

        then:
        empty.@count == 99
    }

    /**
     * Useful to check this, since long and double take up two slots in the stack and may behave
     * differently.
     */
    def "access to existing long field"() {
        def pc = mgr.getPlasticClass(LongFieldHolder.name)

        def f = pc.allFields.first()
        def h = f.handle

        def ins = pc.createInstantiator()

        when:
        def holder = ins.newInstance()

        // Use Groovy to access the field directly

        holder.@value = Long.MAX_VALUE

        then:
        h.get(holder) == Long.MAX_VALUE

        when:
        h.set(holder, 99l)

        then:
        holder.@value == 99l
    }

    /**
     * Default value for fields are set inside the default
     * <init> constructor method; since Plastic creates a new
     * constructor, it must find the existing constructor and
     * execute it, after it does its own initializations.
     */
    def "default value for field is not lost"() {

        def pc = mgr.getPlasticClass(LongFieldHolder.name)

        def f = pc.allFields.first()
        def h = f.handle

        def ins = pc.createInstantiator()
        def holder = ins.newInstance()

        expect:
        h.get(holder) == 100l
    }

    def "access to reference field"() {
        def pc = mgr.getPlasticClass(StringHolder.name)

        def f = pc.allFields.first()
        def h = f.handle

        def holder = pc.createInstantiator().newInstance()

        when:
        String alpha = "alpha"
        String beta = "beta"

        holder.@value = alpha

        then:
        holder.@value.is(alpha)
        h.get(holder).is(alpha)

        when:
        h.set(holder, beta)

        then:
        holder.@value.is(beta)
        h.get(holder).is(beta)
    }

    def "access to multiple fields in single class"() {
        def pc = mgr.getPlasticClass(MultipleFields.name)

        def fred = handleByName(pc, "fred")
        def barney = handleByName(pc, "barney")
        def wilma = handleByName(pc, "wilma")
        def betty = handleByName(pc, "betty")

        def instance = pc.createInstantiator().newInstance()

        when:

        fred.set(instance, 99)

        then:
        instance.fred == 99

        when:

        barney.set(instance, "rubble")

        then:

        instance.barney == "rubble"


        // skip wilma since its a List<Long>

        when:

        betty.set(instance, 101)

        then:

        instance.betty == 101
    }

    def handleByName(pc, name) {
        pc.allFields.find({ it.name == name}).handle
    }

    /**
     * Hit a bug where if a field has a conduit (and therefore, has added methods to access that conduit
     * when the field is read or written) and it has a FieldHandle, the methods needed by the shim
     * (for the FieldHandle) were incorrectly optimized out of existence.
     */
    def "handle to field keeps synthetic access methods from being removed"() {

        def fc = Mock(FieldConduit)

        def pc = mgr.getPlasticClass(FieldHandleAccessOnly.name)

        def field = pc.allFields.first()

        def handle = field.setConduit(fc).handle

        def instance = pc.createInstantiator().newInstance()

        when:

        handle.set(instance, "ok")

        then:

        1 * fc.set(_, _, "ok")
    }
}
