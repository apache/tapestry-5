package org.apache.tapestry.ioc

import org.apache.tapestry5.ioc.EagerProxyReloadModule


class EagerLoadSpec extends IOCSpecification {

    def "proxied service does eager load"()
    {
        expect:

        EagerProxyReloadModule.eagerLoadServiceDidLoad == false

        when:

        buildRegistry EagerProxyReloadModule

        performRegistryStartup()

        then:

        EagerProxyReloadModule.eagerLoadServiceDidLoad == true
    }
}
