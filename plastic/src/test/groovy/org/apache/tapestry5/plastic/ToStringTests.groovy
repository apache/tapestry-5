package org.apache.tapestry5.plastic

import org.apache.tapestry5.internal.plastic.StandardDelegate
import testsubjects.HasToString

class ToStringTests extends AbstractPlasticSpecification {

    def "add toString method to class that does not yet implement it"() {
        setup:

        def mgr = PlasticManager.withContextClassLoader().create()

        def o = mgr.createClass (Object, {
            it.addToString "<ToString>" } as PlasticClassTransformer).newInstance()

        expect:

        o.toString() == "<ToString>"
    }

    def "add toString method to class that does already implement it"() {
        setup:

        // Make sure the base class is transformed (and therefore, the existence of the toString() method noted)

        def mgr = PlasticManager.withContextClassLoader().delegate(new StandardDelegate()).packages(["testsubjects"]).create()

        def o = mgr.createClass (HasToString, { it.addToString "<OverrideToString>" } as PlasticClassTransformer).newInstance()

        expect:

        o.toString() == "<ExistingToString>"
    }
}
