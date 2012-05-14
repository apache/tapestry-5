package org.apache.tapestry5.ioc

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
