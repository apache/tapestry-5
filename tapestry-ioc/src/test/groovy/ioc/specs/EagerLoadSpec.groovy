package ioc.specs

import org.apache.tapestry5.ioc.test.EagerProxyReloadModule

class EagerLoadSpec extends AbstractRegistrySpecification {

  def "proxied service does eager load"() {
    expect:

    EagerProxyReloadModule.eagerLoadServiceDidLoad == false

    when:

    buildRegistry EagerProxyReloadModule

    performRegistryStartup()

    then:

    EagerProxyReloadModule.eagerLoadServiceDidLoad == true
  }
}
