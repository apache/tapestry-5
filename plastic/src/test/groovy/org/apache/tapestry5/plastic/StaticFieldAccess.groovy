package org.apache.tapestry5.plastic

import testinterfaces.Access
import testsubjects.StaticFieldHolder

class StaticFieldAccess extends AbstractPlasticSpecification
{
    def "static fields are readable and writable"()
    {
        setup:
        def pc = mgr.getPlasticClass(StaticFieldHolder.name)

        pc.introduceInterface(Access)

        pc.introduceMethod(new MethodDescription(Object.name, "read")).changeImplementation({
            InstructionBuilder b ->
            b.getStaticField(StaticFieldHolder.name, "VALUE", String).returnResult()
        } as InstructionBuilderCallback)

        pc.introduceMethod(new MethodDescription(void.name, "write", "java.lang.Object")).changeImplementation({
            InstructionBuilder b ->
            b.loadArgument(0).checkcast(String)
            b.putStaticField(StaticFieldHolder.name, "VALUE", String).returnResult()
        } as InstructionBuilderCallback)

        Access o = pc.createInstantiator().newInstance()

        expect:

        o.read().is(o.VALUE)

        when:
        o.write "newvalue"

        then:

        o.VALUE == "newvalue"
    }
}
