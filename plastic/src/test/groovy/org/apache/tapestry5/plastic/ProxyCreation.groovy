package org.apache.tapestry5.plastic

import spock.lang.Specification
import testannotations.Maybe
import testannotations.Truth
import testinterfaces.AnnotationTransfer
import testsubjects.AnnotationTransferImpl

class ProxyCreation extends Specification {
    def "create a proxy"() {
        setup:

        def mgr = new PlasticManager()

        Runnable r = Mock(Runnable)

        def ins = mgr.createProxy(Runnable.class, {
            def f = it.introduceField(Runnable.class, "delegate").inject(r)

            it.introduceMethod(new MethodDescription("void", "run")).delegateTo(f)
        } as PlasticClassTransformer)

        when:

        def proxy = ins.newInstance()

        then:

        assert proxy instanceof Runnable

        when:

        proxy.run()

        then:

        r.run()
    }

    def "type must be an interface"() {
        setup:
        def mgr = new PlasticManager()

        when:

        mgr.createProxy (String.class, {
        } as PlasticClassTransformer)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Class java.lang.String is not an interface; proxies may only be created for interfaces."
    }

    def "copying of annotations"() {
        setup:
        def mgr = new PlasticManager()

        def o = mgr.createProxy (AnnotationTransfer.class, { PlasticClass pc ->

            pc.copyAnnotations (AnnotationTransferImpl.class)
        } as PlasticClassTransformer).newInstance()

        def oc = o.getClass()
        def method1 = oc.getMethod("method1", int.class)

        expect:

        oc.getAnnotation(Maybe.class).value() == Truth.YES

        method1.getAnnotation(Maybe.class).value() == Truth.YES
        method1.getParameterAnnotations()[0][0].annotationType() == Maybe.class
        method1.getParameterAnnotations()[0][0].value() == Truth.YES
    }
}
