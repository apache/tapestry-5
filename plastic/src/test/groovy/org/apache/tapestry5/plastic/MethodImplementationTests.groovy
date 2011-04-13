package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;


class MethodImplementationTests extends AbstractPlasticSpecification {
    def "overwrite implementation of method"() {

        setup:

        def mgr = createMgr ( { PlasticClass pc ->
            def b = findMethod(pc, "getValue").changeImplementation( { b ->
                b.loadConstant(97).returnResult()
            } as InstructionBuilderCallback )
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator("testsubjects.MethodReimplementationSubject").newInstance()

        then:

        o.value == 97
    }

    def "support for constant opcodes"() {
        setup:

        def mgr = createMgr ( { PlasticClass pc ->
            def b = findMethod(pc, "getValue").changeImplementation( { b ->
                b.loadConstant(2).returnResult()
            } as InstructionBuilderCallback )
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator("testsubjects.MethodReimplementationSubject").newInstance()

        then:
        "There's no way to tell from the outside, but should use the ICONST_2 opcode."

        o.value == 2
    }
}
