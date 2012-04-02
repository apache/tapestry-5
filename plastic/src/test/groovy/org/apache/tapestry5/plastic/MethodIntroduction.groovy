package org.apache.tapestry5.plastic

import testsubjects.ChildClass

class MethodIntroduction extends AbstractPlasticSpecification {

    static final String CLASS_NAME = ChildClass.name

    def instanceWithIntroducedMethod(MethodDescription md, isOverride) {
        def mgr = createMgr({ PlasticClass pc ->
            if (pc.className == CLASS_NAME) {
                def method = pc.introduceMethod(md)

                assert method.override == isOverride
            }
        } as PlasticClassTransformer)

        return mgr.getClassInstantiator(CLASS_NAME).newInstance()
    }

    def "introduce method not present in base class"() {

        def o = instanceWithIntroducedMethod(new MethodDescription(returnType, methodName), false)

        when:

        def returned = access.call(o)

        then:

        returned == expectedValue

        expectedValue == null || returned.class.is(expectedType)

        // Groovy gets in the way here as it autoboxes the primitives before we get a chance to see them,
        // thus we check for the wrapper type even though its supposed to be the primitive type
        // that's returned. Meanwhile. the JVM is turning ICONST_0 into char or ints.  Very hard
        // to verify that everything is doing what it should be!

        where:

        returnType         | methodName  | access             | expectedValue | expectedType

        "java.lang.String" | "getString" | { it.getString() } | null          | null
        "java.util.Date[]" | "getDates"  | { it.getDates() }  | null          | null
        "int"              | "getInt"    | { it.getInt() }    | 0             | Integer
        "int[]"            | "getInts"   | { it.getInts() }   | null          | null
        "char"             | "getChar"   | { it.getChar() }   | 0             | Character
        "float"            | "getFloat"  | { it.getFloat() }  | 0f            | Float
        "long"             | "getLong"   | { it.getLong() }   | 0l            | Long
        "double"           | "getDouble" | { it.getDouble() } | 0d            | Double
    }

    def "introduce void method override"() {

        setup:

        def o = instanceWithIntroducedMethod(new MethodDescription("void", "voidMethod"), true)

        when:

        o.voidMethod()

        then:

        true
    }

    def "introduce primitive method override"() {
        setup:

        def o = instanceWithIntroducedMethod(new MethodDescription("int", "primitiveMethod", "int"), true)

        expect:

        o.primitiveMethod(97) == 97
    }

    def "introduce object method override"() {

        setup:

        def o = instanceWithIntroducedMethod(new MethodDescription("java.lang.String", "objectMethod", "java.lang.String"), true)

        expect:

        o.objectMethod("plastic") == "plastic"
    }

    def "introduce interface"() {

        def introduced

        setup:

        def mgr = createMgr({ PlasticClass pc ->
            if (pc.className == CLASS_NAME)
                introduced = pc.introduceInterface(Runnable).collect { it.description }
        } as PlasticClassTransformer)

        when:

        def o = mgr.getClassInstantiator(CLASS_NAME).newInstance()

        then:

        o instanceof Runnable

        o.run()  // Does not fail

        introduced.size() == 1
        introduced[0].methodName == "run"
    }

    def "check for introduced interface is visible in subclasses"() {
        setup:

        boolean present;

        def mgr = createMgr({
            PlasticClass pc ->
            if (pc.className.contains("Base")) {
                pc.introduceInterface(Serializable)
            }

            if (pc.className.contains("Child")) {
                present = pc.isInterfaceImplemented(Serializable)
            }

        } as PlasticClassTransformer)

        when:

        mgr.getClassInstantiator(ChildClass.name)

        then:

        present == true
    }

}

