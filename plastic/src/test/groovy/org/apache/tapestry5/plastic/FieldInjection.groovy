package org.apache.tapestry5.plastic

import testsubjects.Empty
import testsubjects.InjectFieldSubject
import testsubjects.StringHolder
import testsubjects.StringPropertyHolder

class FieldInjection extends AbstractPlasticSpecification
{
    def "injection of a reference value"()
    {
        String injected = "Value injected into the Empty class"

        def pc = mgr.getPlasticClass(Empty.name)

        def f = pc.introduceField(String.name, "stringValue");

        f.inject(injected);

        def ins = pc.createInstantiator()

        def o = ins.newInstance()

        expect:
        o.stringValue.is(injected)
    }

    def "computed injection"()
    {

        def instanceType

        String injected = "Computed value injected into the StringPropertyHolder class"

        def pc = mgr.getPlasticClass(StringPropertyHolder.name)

        pc.allFields.first().injectComputed({

            instanceType = it.instanceType

            return it.get(String)
        } as ComputedValue)

        def o = pc.createInstantiator().with(String, injected).newInstance()

        expect:

        o.value.is(injected)
        instanceType.is(o.getClass())

        !instanceType.is(StringPropertyHolder) // it's a new class with the same name

        when:
        o.value = "attempt to update"

        then:
        thrown(IllegalStateException)
    }

    def "injection from instance context"()
    {

        String injected = "InstanceContext value injected into the StringPropertyHolder class"

        def pc = mgr.getPlasticClass(StringPropertyHolder.name)

        pc.allFields.first().injectFromInstanceContext()

        def o = pc.createInstantiator().with(String, injected).newInstance()

        expect:

        o.value.is(injected)

        when:
        o.value = "attempt to update"

        then:
        thrown(IllegalStateException)
    }

    def "injection of primitive value"()
    {

        def pc = mgr.getPlasticClass(Empty.name)

        def f = pc.introduceField("double", "pi");

        f.inject(Math.PI);

        def ins = pc.createInstantiator()

        def o = ins.newInstance()

        expect:
        o.pi == Math.PI
    }


    def "injected field is read-only"()
    {

        def pc = mgr.getPlasticClass(InjectFieldSubject.name)

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

    def "injected field is read-only, even via handle"()
    {
        def pc = mgr.getPlasticClass(InjectFieldSubject.name)
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

    def "a field may only be injected once"()
    {
        def pc = mgr.getPlasticClass(StringHolder.name)
        def f = pc.allFields.first();

        f.inject("[first]")

        when:

        f.inject("[second]")

        then:

        def e = thrown(IllegalStateException)

        e.message == "Unable to inject a value into field value of class testsubjects.StringHolder, as it already has an injection."
    }


}
