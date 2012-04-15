package org.apache.tapestry.ioc

import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import spock.lang.AutoCleanup
import spock.lang.Specification

abstract class IOCSpecification extends Specification {

    @AutoCleanup("shutdown")
    protected Registry registry;

    protected final void buildRegistry(Class... moduleClasses) {

        registry =
            new RegistryBuilder().add(moduleClasses).build()
    }

    /** Any unrecognized methods are evaluated against the registry. */
    def methodMissing(String name, args) {
        registry."$name"(*args)
    }


}
