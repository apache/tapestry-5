package org.apache.tapestry5.plastic

import testinterfaces.Access

class StaticFieldAccess extends AbstractPlasticSpecification
{
    def "static fields are readable and writable"()
    {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.StaticFieldHolder")

        pc.introduceInterface(Access)

        pc.introduceMethod(new MethodDescription("java.lang.Object", "read")).changeImplementation({
            InstructionBuilder b ->
            b.getStaticField("testsubjects.StaticFieldHolder", "VALUE", String).returnResult()
        } as InstructionBuilderCallback)

        pc.introduceMethod(new MethodDescription("void", "write", "java.lang.Object")).changeImplementation({
            InstructionBuilder b ->
            b.loadArgument(0).checkcast(String)
            b.putStaticField("testsubjects.StaticFieldHolder", "VALUE", String).returnResult()
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
