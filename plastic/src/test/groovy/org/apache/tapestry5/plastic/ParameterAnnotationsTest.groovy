package org.apache.tapestry5.plastic;


import org.apache.tapestry5.plastic.test.TestInject
import testsubjects.ParameterAnnotationsSubject

public class ParameterAnnotationsTest extends AbstractPlasticSpecification {
    def "parameter annotations are visible"() {
        setup:

        def mgr = createMgr()
        PlasticClass pc = mgr.getPlasticClass(ParameterAnnotationsSubject.name)
        PlasticMethod pm = findMethod(pc, "theMethod")

        when:

        def params = pm.parameters

        then:

        params.size() == 2
        
        params[0].type == String.name
        params[0].index == 0
        params[0].hasAnnotation(TestInject)
        params[0].getAnnotation(TestInject) instanceof TestInject
        
        params[1].type == "int"
        params[1].index == 1
        params[1].hasAnnotation(TestInject) == false
       
    }
}
