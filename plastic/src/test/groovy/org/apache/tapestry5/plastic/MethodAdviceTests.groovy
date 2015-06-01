package org.apache.tapestry5.plastic

import org.apache.tapestry5.plastic.test.NoopAdvice
import testannotations.FieldAnnotation
import testannotations.Maybe
import testannotations.MethodAnnotation
import testannotations.Truth
import testinterfaces.MagicContainer
import testsubjects.SingleMethod

import java.sql.SQLException

class MethodAdviceTests extends AbstractPlasticSpecification {

    def "advice for a void method"() {
        setup:

        def didInvoke = false
        def methodId;

        def mgr = createMgr({ PlasticClass pc ->

            def method = findMethod(pc, "aSingleMethod")

            methodId = method.methodIdentifier

            findMethod(pc, "aSingleMethod").addAdvice({
                didInvoke = true

                assert it.method.name == "aSingleMethod"

                assert it.getParameter(0) == 123

                assert it.hasAnnotation(Deprecated) == false
                assert it.hasAnnotation(Maybe) == true

                assert it.getAnnotation(Maybe).value() == Truth.YES

                it.proceed()
            } as MethodAdvice)
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator(SingleMethod.name).newInstance()

        then:

        didInvoke == false

        methodId == "testsubjects.SingleMethod.aSingleMethod(int)"

        when:

        o.aSingleMethod(123)

        then:

        didInvoke == true
    }

    def "multiple advice on method with parameters and return values"() {

        setup:

        def mgr = createMgr({ PlasticClass pc ->
            findMethod(pc, "dupe").addAdvice({

                it.setParameter(0, it.getParameter(0) + 2)
                it.proceed()
            } as MethodAdvice).addAdvice({

                it.setParameter(0, it.getParameter(0) * 3)
                it.proceed()

                it.setReturnValue(it.getReturnValue().toUpperCase())
            } as MethodAdvice)
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.MethodAdviceTarget.name).newInstance()

        expect:

        o.dupe(2, "Fam") == "FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM FAM"
    }

    def "method that throws exceptions"() {

        setup:

        def mgr = createMgr({ PlasticClass pc ->
            findMethod(pc, "maybeThrow").addAdvice(new NoopAdvice())
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.MethodAdviceTarget.name).newInstance()

        expect:

        o.maybeThrow(7L) == 7L

        when:

        o.maybeThrow(0L)

        then:

        thrown(SQLException)
    }

    def "setting return value clears checked exceptions"() {
        def mgr = createMgr({ PlasticClass pc ->
            findMethod(pc, "maybeThrow").addAdvice({ MethodInvocation mi ->

                mi.proceed()

                if (mi.didThrowCheckedException()) {
                    mi.setReturnValue(-1L)
                }
            } as MethodAdvice)
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.MethodAdviceTarget.name).newInstance()

        expect:

        o.maybeThrow(9L) == 9L

        o.maybeThrow(0L) == -1L
    }

    /**
     * This is important because each double/long takes up two local variable slots.
     *
     * @return
     */
    def "method with long and double parameters"() {
        setup:

        def mgr = createMgr({ PlasticClass pc ->
            findMethod(pc, "doMath").addAdvice(new NoopAdvice())
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.WidePrimitives.name).newInstance()

        expect:
        "The interceptor builds proper bytecode to pass the values through"

        o.doMath(2l, 4.0d, 5, 6l) == 38d
    }

    def "method advice does not interfere with field instrumentation (advice first)"() {
        FieldConduit fc = [get: { instance, context ->
            return "via conduit"
        }, set: { instance, context -> }] as FieldConduit

        MethodAdvice justProceed = { inv -> inv.proceed() } as MethodAdvice

        def mgr = createMgr({ PlasticClass pc ->

            pc.getMethodsWithAnnotation(MethodAnnotation).each({ m ->
                m.addAdvice(justProceed)
            })

            pc.getFieldsWithAnnotation(FieldAnnotation).each({ f ->
                f.setConduit(fc)
            })
        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.FieldConduitInsideAdvisedMethod.name).newInstance()

        expect:

        o.magic == "via conduit"
    }

    def "method advice does not interfere with field instrumentation (conduit first)"() {
        FieldConduit fc = [get: { instance, context ->
            return "via conduit"
        }, set: { instance, context -> }] as FieldConduit

        MethodAdvice justProceed = { inv -> inv.proceed() } as MethodAdvice

        def mgr = createMgr({ PlasticClass pc ->

            pc.getFieldsWithAnnotation(FieldAnnotation).each({ f ->
                f.setConduit(fc)
            })

            pc.getMethodsWithAnnotation(MethodAnnotation).each({ m ->
                m.addAdvice(justProceed)
            })

        } as PlasticClassTransformer)

        def o = mgr.getClassInstantiator(testsubjects.FieldConduitInsideAdvisedMethod.name).newInstance()

        expect:

        o.magic == "via conduit"
    }

    def "method advice does not interfere with field instrumentation (instance context version)"() {
        MagicContainer container = Mock()

        FieldConduit fc = [get: { instance, context ->

            return context.get(MagicContainer).magic()

        }, set: { instance, context -> }] as FieldConduit

        MethodAdvice justProceed = { inv -> inv.proceed() } as MethodAdvice

        def mgr = createMgr({ PlasticClass pc ->

            pc.getMethodsWithAnnotation(MethodAnnotation).each({ m ->
                m.addAdvice(justProceed)
            })

            pc.getFieldsWithAnnotation(FieldAnnotation).each({ f ->
                f.setConduit(fc)
            })
        } as PlasticClassTransformer)

        if (false) {
            enableBytecodeDebugging(mgr)
        }

        def o = mgr.getClassInstantiator(testsubjects.FieldConduitInsideAdvisedMethod.name).with(MagicContainer, container).newInstance()

        when:

        o.magic == "via context and mock"

        then:

        1 * container.magic() >> "via context and mock"
    }

    def "method advice on method that accesses a field with a conduit (more complex structure)"() {
        MagicContainer container = Mock()

        FieldConduit fc = [get: { instance, context ->

            return context.get(MagicContainer)

        }, set: { instance, context -> }] as FieldConduit

        MethodAdvice justProceed = { inv -> inv.proceed() } as MethodAdvice

        def mgr = createMgr({ PlasticClass pc ->

            pc.getMethodsWithAnnotation(MethodAnnotation).each({ m ->
                m.addAdvice(justProceed)
            })

            pc.getFieldsWithAnnotation(FieldAnnotation).each({ f ->
                f.setConduit(fc)
            })
        } as PlasticClassTransformer)

        if (false) {
            enableBytecodeDebugging(mgr)
        }

        def o = mgr.getClassInstantiator(testsubjects.FieldConduitAdvisedMethodComplexCase.name).with(MagicContainer, container).newInstance()

        when:

        o.magic == "via context"

        then:

        1 * container.magic() >> "via context"
    }

    def "method advice added before changing method implementation does not wipe out the method implementation"() {
        def mgr = createMgr({ PlasticClass pc ->
            def methods = pc.introduceInterface(MagicContainer)

            methods.each { PlasticMethod m ->
                m.addAdvice({ MethodInvocation iv ->
                    iv.proceed()

                    // Can't use a GString here as the unevaluated GString gets assigned, causing a
                    // ClassCastException
                    iv.returnValue = String.format("<<%s>>", iv.returnValue)
                } as MethodAdvice)

                m.changeImplementation({ InstructionBuilder b ->

                    b.loadConstant "MAGIC!"
                    b.returnResult()

                } as InstructionBuilderCallback)
            }
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator(testsubjects.AdviceAndImplementationSubject.name).newInstance()

        then:

        // This demonstrates that the implementation was changed and the advice wrapped around
        // the new implementation. Before fixing TAP5-1979 this would be "<<null>>".

        o.magic() == "<<MAGIC!>>"
    }

    def "abstract methods are identified as such by PlasticMethod"() {

        setup:

        PlasticClass pc = mgr.getPlasticClass(testsubjects.AbstractPlaceholder.name)

        PlasticMethod m = findMethod pc, "placeholder"

        expect:

        m.isAbstract() == true
    }

    def "implementations of abstract methods are considered to be overrides"() {
        setup:

        def packages = ["testsubjects"] as Set

        PlasticManager mgr = PlasticManager.withContextClassLoader().packages(packages).create()

        PlasticClass pc = mgr.getPlasticClass(testsubjects.PlaceholderImpl.name)

        PlasticMethod m = findMethod pc, "placeholder"

        expect:

        m.isOverride() == true
    }

    def "ignores Error and RuntimeException in throws declaration"() {

        def mgr = createMgr({ PlasticClass pc ->
            def methods = pc.getMethodsWithAnnotation(MethodAnnotation)

            methods.each { PlasticMethod m ->
                m.addAdvice({ MethodInvocation iv ->
                    iv.proceed()
                } as MethodAdvice)
            }
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator(testsubjects.DeclaredExceptions.name).newInstance()

        o.throwsRuntime()

        then:

        def e = thrown(RuntimeException)

        e.message == "throwsRuntime";

        when:

        o.throwsError()

        then:

        e = thrown(Error)

        e.message == "throwsError"

        when:

        o.throwsException()

        then:

        e = thrown(Exception)

        e.message == "throwsException"
    }
}
