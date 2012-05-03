package org.apache.tapestry5.ioc

import org.apache.tapestry5.ioc.internal.services.StartupModule2

class RegistrySpec extends AbstractRegistrySpecification {

  def "symbol in Registry.getService() is expanded"() {

    buildRegistry GreeterModule

    when:

    def greeter = getService '${greeter}', Greeter

    then:

    greeter.greeting == "Hello"
    greeter.toString() == "<Proxy for HelloGreeter(org.apache.tapestry5.ioc.Greeter)>"
  }

  def "circular module references are ignored"() {
    buildRegistry HelterModule

    when:

    def helter = getService "Helter", Runnable
    def skelter = getService "Skelter", Runnable

    then:

    !helter.is(skelter)
  }

  def "@Startup annotation support"() {
    when:

    buildRegistry StartupModule2

    then:

    !StartupModule2.staticStartupInvoked
    !StartupModule2.instanceStartupInvoked

    when:

    performRegistryStartup()

    then:

    StartupModule2.staticStartupInvoked
    StartupModule2.instanceStartupInvoked
  }
}
