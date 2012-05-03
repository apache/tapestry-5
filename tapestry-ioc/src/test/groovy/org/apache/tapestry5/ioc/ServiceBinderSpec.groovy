package org.apache.tapestry5.ioc


class ServiceBinderSpec extends AbstractRegistrySpecification {

  def "a service implementation may be created via a ServiceBuilder callback"() {
    buildRegistry ServiceBuilderModule

    when:

    def g = getService "Greeter", Greeter

    then:
    g.greeting == "Greetings from service Greeter."
  }

  def "verify exception reporting for ServiceBuilder that throws an exception"() {

    buildRegistry ServiceBuilderModule

    def g = getService "BrokenGreeter", Greeter

    when:

    g.greeting

    then:

    Exception e = thrown()

    e.message.contains "Exception constructing service 'BrokenGreeter'"
    e.message.contains "Failure inside ServiceBuilder callback."
  }

}
