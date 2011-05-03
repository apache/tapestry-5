package org.apache.tapestry5.plastic

import testsubjects.Memory

class MethodProxying extends AbstractPlasticSpecification {

    def "basic proxying"() {
        setup:

        def mockRunnable = Mock(Runnable.class)

        def o = mgr.createClass (Object.class, { PlasticClass pc ->

            def field = pc.introduceField (Runnable.class, "delegate").inject(mockRunnable)

            pc.proxyInterface (Runnable.class, field)
        } as PlasticClassTransformer).newInstance()

        expect:

        Runnable.class.isInstance o

        when:

        o.run()

        then:

        1 * mockRunnable.run()
    }

    def "proxy method with arguments and return value"() {
        setup:

        def handle
        def methodToString

        def o = mgr.createClass(Object.class, { PlasticClass pc ->

            def field = pc.introduceField(Memory.class, "delegate").inject(new Memory())

            def m = Memory.class.getMethod("returnLast", long.class)

            def pm = pc.introduceMethod(m)

            handle = pm.delegateTo(field).handle
            methodToString = pm.toString()
        } as PlasticClassTransformer).newInstance()

        expect:

        handle.invoke(o, 5l).returnValue == 0l
        handle.invoke(o, 10l).returnValue == 5l
        handle.invoke(o, 50l).returnValue == 10l

        // A sideline, but still good to know

        methodToString ==~ /PlasticMethod\[public long returnLast\(long\) in class .*/

        handle.toString() ==~ /MethodHandle\[.* public long returnLast\(long\)\]/
    }

    def "proxy method using dynamic delegate via method"() {
        setup:

        def handle

        def o = mgr.createClass(Object.class, { PlasticClass pc ->

            def memory = new Memory()

            PlasticMethod providerMethod = pc.introduceMethod(new MethodDescription(Memory.class.name, "provideDelegate")).addAdvice({ MethodInvocation mi ->

                mi.returnValue = memory

                // And don't proceed()

            } as MethodAdvice)

            def m = Memory.class.getMethod("returnLast", long.class)

            def pm = pc.introduceMethod(m)

            handle = pm.delegateTo(providerMethod).handle
        } as PlasticClassTransformer).newInstance()

        expect:

        handle.invoke(o, 5l).returnValue == 0l
        handle.invoke(o, 10l).returnValue == 5l
        handle.invoke(o, 50l).returnValue == 10l
    }
}
