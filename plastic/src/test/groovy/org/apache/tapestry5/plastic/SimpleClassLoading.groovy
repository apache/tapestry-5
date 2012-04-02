package org.apache.tapestry5.plastic

import testsubjects.InjectionSubject
import testsubjects.InjectionSubjectSubclass
import testsubjects.TestInjectTransformer

import java.util.concurrent.Callable

/**
 * This is the first test that integrates the class loader with the PlasticManager, simulating how Plastic will
 * be used in the wild.
 */
class SimpleClassLoading extends AbstractPlasticSpecification {


    def "basic field transformation"() {
        setup:

        def concreteClass = InjectionSubject
        def goHandle
        def runnable = Mock(Runnable)
        def transformer = new TestInjectTransformer(Runnable, runnable)

        def mgr = createMgr(transformer, {
            goHandle = findHandle(it, "go")
        } as PlasticClassTransformer);


        def ins = mgr.getClassInstantiator(InjectionSubject.name)
        def instance = ins.newInstance()

        when:

        goHandle.invoke instance

        then:

        1 * runnable.run()

        concreteClass != instance
    }

    def "field transformation in subclass of transformed class"() {
        setup:

        def goHandle
        def callHandle
        def runnable = Mock(Runnable)
        def callable = Mock(Callable)

        def mgr = createMgr(
                new TestInjectTransformer(Runnable, runnable),
                new TestInjectTransformer(Callable, callable), {
                    if (goHandle == null) goHandle = findHandle(it, "go")

                    if (callHandle == null) callHandle = findHandle(it, "call")
                } as PlasticClassTransformer)


        def ins = mgr.getClassInstantiator(InjectionSubjectSubclass.name)
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
