package ioc.specs

import org.apache.tapestry5.ioc.test.DuplicateFredModule
import org.apache.tapestry5.ioc.test.ExtraMethodsModule
import org.apache.tapestry5.ioc.test.FredModule
import org.apache.tapestry5.ioc.test.NoImplementationClassForSimpleIdModule
import org.apache.tapestry5.ioc.test.RecursiveConstructorModule
import org.apache.tapestry5.ioc.test.UnknownScopeModule
import org.apache.tapestry5.ioc.test.internal.ExtraPublicConstructorsModule
import org.apache.tapestry5.ioc.test.internal.PrivateConstructorModule
import org.apache.tapestry5.ioc.test.internal.UpcaseService

/**
 * A few tests that are easiest (or even just possible) by building a Registry and trying out a few
 * things.
 */
class RegistryConstructionAndRuntimeErrorsSpec extends AbstractRegistrySpecification {

  def "duplicate service names are a failure"() {
    when:

    buildRegistry FredModule, DuplicateFredModule

    then:

    RuntimeException ex = thrown()

    ex.message.startsWith "Service id 'Fred' has already been defined by"

    // Can't check entire message, can't guarantee what order modules will be processed in
  }

  def "service with unknown scope fails at service proxy creation"() {
    buildRegistry UnknownScopeModule

    when:

    getService "UnknownScope", Runnable

    then:

    Exception e = thrown()

    e.message.contains "Error building service proxy for service 'UnknownScope'"
    e.message.contains "Unknown service scope 'magic'"
  }

  def "ensure that recursive module construction is detected"() {

    buildRegistry RecursiveConstructorModule

    def runnable = getService "Runnable", Runnable

    when:

    // We can get the proxy, but invoking a method causes
    // the module to be instantiated ... but that also invokes a method on
    // the proxy.

    runnable.run()

    then:

    RuntimeException e = thrown()

    e.message.contains "has failed due to recursion"
  }

  def "a module class must have a public constructor"() {

    buildRegistry PrivateConstructorModule

    def trigger = getService "Trigger", Runnable

    when:

    trigger.run()

    then:

    RuntimeException e = thrown()

    e.message.contains "Module class org.apache.tapestry5.ioc.test.internal.PrivateConstructorModule does not contain any public constructors."
  }

  def "extra public constructors on a module class are ignored"() {

    buildRegistry ExtraPublicConstructorsModule

    when: "forcing the module to be instantiated"

    def upcase = getService UpcaseService

    then: "no exception when instantiating the module"

    upcase.upcase('Hello, ${fred}') == "HELLO, FLINTSTONE"
  }

  def "extra public methods on module classes are exceptions"() {
    when:
    buildRegistry ExtraMethodsModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Module class org.apache.tapestry5.ioc.test.ExtraMethodsModule contains unrecognized public methods: "
    e.message.contains "thisMethodIsInvalid()"
    e.message.contains "soIsThisMethod()"
  }

  def "can not use withSimpleId() when binding a service interface to a ServiceBuilder callback"() {
    when:

    buildRegistry NoImplementationClassForSimpleIdModule

    then:

    RuntimeException e = thrown()

    e.message.contains "No defined implementation class to generate simple id from"
  }
}
