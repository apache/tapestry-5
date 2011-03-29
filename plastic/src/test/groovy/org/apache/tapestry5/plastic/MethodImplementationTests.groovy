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
}
