package org.apache.tapestry5.plastic

import org.apache.tapestry5.internal.plastic.StandardDelegate
import spock.lang.Specification

abstract class AbstractPlasticSpecification extends Specification
{

    def mgr = PlasticManager.withContextClassLoader().create()

    PlasticMethod findMethod(pc, name)
    {
        pc.methods.find { it.description.methodName == name }
    }

    MethodHandle findHandle(pc, name)
    {
        findMethod(pc, name)?.handle
    }

    PlasticManager createMgr(PlasticClassTransformer... transformers)
    {
        def delegate = new StandardDelegate(transformers)
        def packages = ["testsubjects"] as Set

        return PlasticManager.withContextClassLoader().delegate(delegate).packages(packages).create()
    }

    void enableBytecodeDebugging(PlasticManager mgr)
    {
        mgr.addPlasticClassListener({ PlasticClassEvent event ->

            print(event.getDissasembledBytecode())

        } as PlasticClassListener)
    }
}
