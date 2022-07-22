package ioc.specs

import org.apache.tapestry5.ioc.test.EagerProxyReloadModule

class EagerLoadSpec extends AbstractRegistrySpecification {

  def "proxied service does eager load"() {
    expect:

    EagerProxyReloadModule.eagerLoadService1DidLoad == false
    EagerProxyReloadModule.nonProxyEagerLoadServiceDidLoad == false
    EagerProxyReloadModule.eagerLoadService2DidLoad == false

    when:

    buildRegistry EagerProxyReloadModule

    performRegistryStartup()

    then:

    EagerProxyReloadModule.eagerLoadService1DidLoad == true
    EagerProxyReloadModule.nonProxyEagerLoadServiceDidLoad == true
    EagerProxyReloadModule.eagerLoadService2DidLoad == true
  }
}
