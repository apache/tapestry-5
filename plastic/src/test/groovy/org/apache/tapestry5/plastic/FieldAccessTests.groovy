package org.apache.tapestry5.plastic

import testannotations.KindaInject
import testannotations.SimpleAnnotation
import testsubjects.ProtectedField
import testsubjects.ProtectedFieldCollaborator

/**
 *  Tests related to access to non-private fields between transformed classes (a new feature in 5.4).
 */
class FieldAccessTests extends AbstractPlasticSpecification {
    def "access protected field from other transformed class"() {
        FieldConduit fc = Mock()

        PlasticClass pc = mgr.getPlasticClass(ProtectedField.name)

        pc.allFields.first().setConduit(fc)

        def delegate = pc.createInstantiator().newInstance()

        pc = mgr.getPlasticClass(ProtectedFieldCollaborator.name)

        pc.allFields.first().inject(delegate)

        def collab = pc.createInstantiator().newInstance()

        when:

        collab.setProtectedValue("gloop")

        then:

        1 * fc.set(_, _, "gloop")


        when:

        assert collab.getProtectedValue() == "badoop"

        then:

        fc.get(_, _) >> "badoop"
    }

    def "access protected field from inner class"() {

        FieldConduit fc = Mock()

        def delegate

        PlasticClassTransformer installFieldConduit = {     PlasticClass pc ->

            pc.getFieldsWithAnnotation(SimpleAnnotation).each { f -> f.setConduit(fc) }

        } as PlasticClassTransformer

        PlasticClassTransformer handleInjection = { PlasticClass pc ->

            pc.getFieldsWithAnnotation(KindaInject).each { f -> f.inject(delegate) }
        } as PlasticClassTransformer

        def mgr = createMgr(installFieldConduit, handleInjection)

        delegate = mgr.getClassInstantiator(ProtectedField.name).newInstance()

        def collab = mgr.getClassInstantiator(ProtectedFieldCollaborator.name).newInstance()

        when:

        fc.get(_, _) >> "gnip"

        then:

        collab.valueGetter.value == "gnip"
    }
}
