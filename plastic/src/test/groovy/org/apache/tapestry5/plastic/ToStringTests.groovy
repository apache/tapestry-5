package org.apache.tapestry5.plastic

import org.apache.tapestry5.internal.plastic.StandardDelegate

import testsubjects.HasToString

class ToStringTests extends AbstractPlasticSpecification {

    def "add toString method to class that does not yet implement it"() {
        setup:

        PlasticManager mgr = new PlasticManager()

        def o = mgr.createClass (Object.class, { it.addToString "<ToString>" } as PlasticClassTransformer).newInstance()

        expect:

        o.toString() == "<ToString>"
    }

    def "add toString method to class that does already implement it"() {
        setup:

        // Make sure the base class is transformed (and therefore, the existence of the toString() method noted)

        def mgr = new PlasticManager (Thread.currentThread().contextClassLoader, new StandardDelegate(), ["testsubjects"]as Set)

        def o = mgr.createClass (HasToString.class, { it.addToString "<OverrideToString>" } as PlasticClassTransformer).newInstance()

        expect:

        o.toString() == "<ExistingToString>"
    }
}
