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

import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.PlasticManager;

import spock.lang.Specification;
import testsubjects.StringPropertyHolder;

class FieldInjection extends Specification
{
    def mgr = new PlasticManager()

    def "injection of a reference value"() {
        String injected = "Value injected into the Empty class"

        def pc = mgr.getPlasticClass("testsubjects.Empty")

        def f = pc.introduceField("java.lang.String", "stringValue");

        f.inject(injected);

        def ins = pc.createInstantiator()

        def o  = ins.newInstance()

        expect:
        o.stringValue.is(injected)
    }

    def "computed injection"() {

        def instanceType

        String injected = "Computed value injected into the StringPropertyHolder class"

        def pc = mgr.getPlasticClass("testsubjects.StringPropertyHolder")

        pc.allFields.first().injectComputed({

            instanceType = it.instanceType

            return it.get(String.class)
        } as ComputedValue)

        def o = pc.createInstantiator().with(String.class, injected).newInstance()

        expect:

        o.value.is(injected)
        instanceType.is(o.getClass())

        ! instanceType.is(StringPropertyHolder.class) // it's a new class with the same name

        when:
        o.value = "attempt to update"

        then:
        thrown(IllegalStateException)
    }

    def "injection from instance context"() {

        String injected = "InstanceContext value injected into the StringPropertyHolder class"

        def pc = mgr.getPlasticClass("testsubjects.StringPropertyHolder")

        pc.allFields.first().injectFromInstanceContext()

        def o = pc.createInstantiator().with(String.class, injected).newInstance()

        expect:

        o.value.is(injected)

        when:
        o.value = "attempt to update"

        then:
        thrown(IllegalStateException)
    }

    def "injection of primitive value"() {

        def pc = mgr.getPlasticClass("testsubjects.Empty")

        def f = pc.introduceField("double", "pi");

        f.inject(Math.PI);

        def ins = pc.createInstantiator()

        def o  = ins.newInstance()

        expect:
        o.pi == Math.PI
    }


    def "injected field is read-only"() {

        def pc = mgr.getPlasticClass("testsubjects.InjectFieldSubject")

        def pf = pc.allFields.first();

        def handle = pf.handle;

        pf.inject(99)

        def ins = pc.createInstantiator()
        def o = ins.newInstance()

        expect:
        o.getValue() == 99

        when:
        o.setValue(123)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Field value of class testsubjects.InjectFieldSubject is read-only."

        when:
        handle.set(o, 456)

        then:

        thrown(IllegalStateException)
    }

    def "injected field is read-only, even via handle"() {
        def pc = mgr.getPlasticClass("testsubjects.InjectFieldSubject")
        def f = pc.allFields.first();
        def h = f.handle

        f.inject(66)

        def ins = pc.createInstantiator()
        def o = ins.newInstance()

        expect:
        o.getValue() == 66

        when:
        h.set(o, 123)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Field value of class testsubjects.InjectFieldSubject is read-only."
    }

    def "a field may only be injected once"() {
        def pc = mgr.getPlasticClass("testsubjects.StringHolder")
        def f = pc.allFields.first();

        f.inject("[first]")

        when:

        f.inject("[second]")

        then:

        def e = thrown(IllegalStateException)

        e.message == "Unable to inject a value into field value of class testsubjects.StringHolder, as it already has an injection."
    }
}
