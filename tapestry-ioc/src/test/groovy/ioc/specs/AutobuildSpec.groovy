package ioc.specs

import org.apache.tapestry5.ioc.test.AutobuildInjectionModule
import org.apache.tapestry5.ioc.test.AutobuildModule
import org.apache.tapestry5.ioc.test.ConventionFailureModule
import org.apache.tapestry5.ioc.test.ConventionModule
import org.apache.tapestry5.ioc.test.ConventionModuleImplementationNotFound
import org.apache.tapestry5.ioc.test.Pingable
import org.apache.tapestry5.ioc.test.ServiceBuilderAutobuilderModule
import org.apache.tapestry5.ioc.test.StringHolder
import org.apache.tapestry5.ioc.test.StringHolderImpl
import org.apache.tapestry5.ioc.test.StringTransformer
import org.apache.tapestry5.ioc.test.UnbuildablePingable
import org.apache.tapestry5.ioc.test.internal.ExceptionInConstructorModule

class AutobuildSpec extends AbstractRegistrySpecification {

  def "ensure that services defined with bind() are automatically built"() {
    buildRegistry AutobuildModule

    def sh = getService StringHolder

    when:

    sh.value = "Foo"

    then:

    sh.value == "Foo"
  }

  def "check reporting of exception in autobuilt service constructor"() {
    buildRegistry ExceptionInConstructorModule

    def pingable = getService Pingable

    when:

    pingable.ping()

    then:

    RuntimeException e = thrown()

    e.message.contains "Error invoking constructor"
    e.message.contains "ExceptionInConstructorServiceImpl()"
    e.message.contains "Yes, we have no tomatoes."
  }

  def "bind() finds the default Impl class"() {
    buildRegistry ConventionModule

    def holder = getService StringHolder

    when:

    // This proves we can invoke methods, meaning that an implementation was found.

    holder.value = "Bar"

    then:

    holder.value == "Bar"
  }

  def "validate exception reporting for no default implementation found"() {
    when:

    buildRegistry ConventionModuleImplementationNotFound

    then:

    RuntimeException e = thrown()
    e.message.contains "Could not find default implementation class org.apache.tapestry5.ioc.test.StringTransformerImpl."
    e.message.contains "Please provide this class, or bind the service interface to a specific implementation class."
  }

  def "validate exception reporting for incorrect implementation (that does not implement service interface)"() {
    when:

    buildRegistry ConventionFailureModule

    then:

    RuntimeException e = thrown()

    e.message.contains "No service implements the interface ${Pingable.name}"
  }

  def "service builder method can use ServiceResources.autobuild()"() {
    buildRegistry ServiceBuilderAutobuilderModule

    def holder = getService StringHolder

    when:

    holder.value = "Foo"

    then:

    holder.value == "Foo"
  }

  def "ensure that Registry.autobuild() works"() {

    buildRegistry()

    when:

    def holder = autobuild StringHolderImpl

    // This test should go further and verify that injections into StringHolderImpl work.

    then:

    holder.class == StringHolderImpl

    when:

    holder.value = "Foo"

    then:

    holder.value == "Foo"
  }

  def "ensure that Registry.autobuild() works (with a description)"() {
    buildRegistry()

    when:

    def holder = autobuild "Building StringHolderImpl", StringHolderImpl

    // This test should go further and verify that injections into StringHolderImpl work.

    then:

    holder.class == StringHolderImpl
  }

  def "verify exception when autobuild service implementation is not valid"() {
    buildRegistry ServiceBuilderAutobuilderModule

    def pingable = getService Pingable

    when:

    pingable.ping()

    then:

    RuntimeException e = thrown()

    e.message.contains "Class org.apache.tapestry5.ioc.test.UnbuildablePingable does not contain a public constructor needed to autobuild."
  }

  def "verify exception when autobuild class has not valid constructor"() {
    buildRegistry()

    when:

    autobuild UnbuildablePingable

    then:

    RuntimeException e = thrown()

    e.message.contains "Class org.apache.tapestry5.ioc.test.UnbuildablePingable does not contain a public constructor needed to autobuild."
  }

  def "a service build method may include a parameter with @Autobuild"() {
    buildRegistry AutobuildInjectionModule

    when:

    def tx = getService StringTransformer

    then:

    tx.transform('Hello, ${fred}') == "Hello, flintstone"
  }
}
