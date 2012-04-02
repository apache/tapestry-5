package org.apache.tapestry5.plastic

import testsubjects.Empty

class ConstructorCallbackTests extends AbstractPlasticSpecification
{
    def "constructor callback invoked after field injection"()
    {
        String injectedValue = "value to inject into new field"
        String observedValue
        FieldHandle fieldHandle

        def mgr = createMgr({ PlasticClass pc ->

            fieldHandle = pc.introduceField(String, "newField").inject(injectedValue).handle

            pc.onConstruct({ instance, context ->

                observedValue = fieldHandle.get(instance)

            } as ConstructorCallback)
        } as PlasticClassTransformer)

        when:

        mgr.getClassInstantiator(Empty.name).newInstance()

        then:

        observedValue == injectedValue
    }
}
