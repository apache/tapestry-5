package org.apache.tapestry5.plastic

/**
 *  Tests related to access to non-private fields between transformed classes (a new feature in 5.4).
 */
class FieldAccessTests extends AbstractPlasticSpecification
{
    def "access protected field from other transformed class"()
    {
        FieldConduit fc = Mock()

        PlasticClass pc = mgr.getPlasticClass("testsubjects.ProtectedField")

        pc.allFields.first().setConduit(fc)

        def delegate = pc.createInstantiator().newInstance()

        pc = mgr.getPlasticClass("testsubjects.ProtectedFieldCollaborator")

        pc.allFields.first().inject(delegate)

        def collab = pc.createInstantiator().newInstance()

        when:

        collab.setProtectedValue("gloop")

        then:

        1 * fc.set(_, _, "gloop")


        when:

        fc.get(_, _) >> "badoop"

        then:

        collab.getProtectedValue() == "badoop"
    }
}
