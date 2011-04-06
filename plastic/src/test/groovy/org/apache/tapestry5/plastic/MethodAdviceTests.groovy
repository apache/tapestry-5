package org.apache.tapestry5.plastic

import java.sql.SQLException

import org.apache.tapestry5.plastic.test.NoopAdvice

class MethodAdviceTests extends AbstractPlasticSpecification {
    def "advice for a void method"() {
        setup:

        def didInvoke = false

        def mgr = createMgr( { PlasticClass pc ->

            findMethod(pc, "aSingleMethod").addAdvice ({
                didInvoke = true
                
                assert it.methodName == "aSingleMethod"
                
                it.proceed()                
            } as MethodAdvice)
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator("testsubjects.SingleMethod").newInstance()

        then:

        didInvoke == false

        when:

        o.aSingleMethod()

        then:

        didInvoke == true
    }

    def "multiple advice on method with parameters and return values"() {

        setup:

        def mgr = createMgr( { PlasticClass pc ->
            findMethod(pc, "dupe").addAdvice( {

                it.setParameter(0, it.getParameter(0) + 2)
                it.proceed()
            } as MethodAdvice).addAdvice ( {

                it.setParameter(0, it.getParameter(0) * 3)
                it.proceed()

                it.setReturnValue(it.getReturnValue().toUpperCase())
            } as MethodAdvice)
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator("testsubjects.MethodAdviceTarget").newInstance()

        expect:

        o.dupe(2, "Fam") == "FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM"
    }
    
    def "method that throws exceptions"() {
        
        setup:
        
        def mgr = createMgr({ PlasticClass pc ->
            findMethod(pc, "maybeThrow").addAdvice(new NoopAdvice())
        } as PlasticClassTransformer)
        
        def o = mgr.getClassInstantiator("testsubjects.MethodAdviceTarget").newInstance()

        expect:
        
        o.maybeThrow(7) == 7
        
        when:
        
        o.maybeThrow(0)
        
        then: 
        
        thrown(SQLException)
                
    }
}
