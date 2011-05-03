package org.apache.tapestry5.plastic


class ProxyCreation extends AbstractPlasticSpecification {
    def "create a field delegating proxy"() {
        setup:

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

    def "create a method delegating proxy"() {
        setup:
        "Each method of the interface delegates through a method that returns the new target for the method."

        Runnable r = Mock(Runnable)

        def ins = mgr.createProxy(Runnable.class, {
            PlasticClass pc ->
            def f = pc.introduceField(Runnable.class, "delegate").inject(r)

            PlasticMethod dm = pc.introducePrivateMethod(Runnable.class.name, "run", null, null)

            assert dm.description.methodName != "run"

            dm.changeImplementation({
                it.loadThis().getField(f).returnResult()
            } as InstructionBuilderCallback)

            Runnable.class.methods.each( { m -> pc.introduceMethod(m).delegateTo(dm) })

            pc.introduceMethod(new MethodDescription("void", "run")).delegateTo(f)
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
        when:

        mgr.createProxy (String.class, {
        } as PlasticClassTransformer)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Class java.lang.String is not an interface; proxies may only be created for interfaces."
    }


    def "listen to plastic class events"() {
        Runnable r = Mock(Runnable)
        PlasticClassListener listener = { PlasticClassEvent event ->

            assert event.className == event.primaryClassName

            assert event.dissasembledBytecode.contains(event.primaryClassName)

            assert event.type == ClassType.PRIMARY
        } as PlasticClassListener

        mgr.addPlasticClassListener listener

        def ins = mgr.createProxy(Runnable.class, {
            def f = it.introduceField(Runnable.class, "delegate").inject(r)

            it.introduceMethod(new MethodDescription("void", "run")).delegateTo(f)
        } as PlasticClassTransformer)

        when:

        def proxy = ins.newInstance()

        then:

        proxy != null
    }
}
