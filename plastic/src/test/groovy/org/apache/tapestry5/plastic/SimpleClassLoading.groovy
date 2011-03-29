package org.apache.tapestry5.plastic

import java.util.concurrent.Callable

import org.apache.tapestry5.plastic.PlasticClassTransformer;

import testsubjects.InjectionSubject
import testsubjects.TestInjectTransformer

/**
 * This is the first test that integrates the class loader with the PlasticManager, simulating how Plastic will
 * be used in the wild.
 */
class SimpleClassLoading extends AbstractPlasticSpecification {


    def "basic field transformation"() {
        setup:

        def concreteClass = InjectionSubject.class
        def goHandle
        def runnable = Mock(Runnable.class)
        def transformer = new TestInjectTransformer(Runnable.class, runnable)

        def mgr = createMgr (transformer, {
            goHandle = findHandle (it, "go")
        } as PlasticClassTransformer);


        def ins = mgr.getClassInstantiator ("testsubjects.InjectionSubject")
        def instance = ins.newInstance()

        when:

        goHandle.invoke instance

        then:

        1 * runnable.run()

        concreteClass != instance.class
    }

    def "field transformation in subclass of transformed class"() {
        setup:

        def goHandle
        def callHandle
        def runnable = Mock(Runnable.class)
        def callable = Mock(Callable.class)

        def mgr = createMgr (
                new TestInjectTransformer(Runnable.class, runnable),
                new TestInjectTransformer(Callable.class, callable), {
                    if (goHandle ==null) goHandle = findHandle (it, "go")

                    if (callHandle == null) callHandle = findHandle(it, "call")
                } as PlasticClassTransformer)


        def ins = mgr.getClassInstantiator ("testsubjects.InjectionSubjectSubclass")
        def instance = ins.newInstance()

        when:

        goHandle.invoke instance

        then:

        1 * runnable.run()

        when:

        callHandle.invoke instance

        then:

        1 * callable.call()
    }
}
