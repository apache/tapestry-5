package ioc.specs

import org.apache.tapestry5.ioc.test.CyclicMOPModule
import org.apache.tapestry5.ioc.test.FieldResourceInjectionModule
import org.apache.tapestry5.ioc.test.FieldResourceService
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.GreeterModule
import org.apache.tapestry5.ioc.test.GreeterServiceOverrideModule
import org.apache.tapestry5.ioc.test.PostInjectionMethodModule

class InjectionSpec extends AbstractRegistrySpecification {

  def "symbol in @Inject is expanded"() {

    buildRegistry GreeterModule

    when:

    def greeter = getService "Greeter", Greeter

    then:

    greeter.greeting == "Hello"
    greeter.toString() == "<Proxy for Greeter(org.apache.tapestry5.ioc.test.Greeter)>"
  }

  def "injection by marker with single match"() {

    buildRegistry GreeterModule

    when:

    def greeter = getService "InjectedBlueGreeter", Greeter

    then:

    greeter.greeting == "Blue"
  }

  def "verify exception for inject by marker with multiple matches"() {
    buildRegistry GreeterModule

    def greeter = getService "InjectedRedGreeter", Greeter

    when:

    greeter.greeting

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to locate a single service assignable to type org.apache.tapestry5.ioc.test.Greeter with marker annotation(s) org.apache.tapestry5.ioc.test.RedMarker"
    e.message.contains "org.apache.tapestry5.ioc.test.GreeterModule.buildRedGreeter1()"
    e.message.contains "org.apache.tapestry5.ioc.test.GreeterModule.buildRedGreeter2()"
  }

  def "verify exception for injection by marker and no matches"() {
    buildRegistry GreeterModule

    def greeter = getService "InjectedYellowGreeter", Greeter

    when:

    greeter.greeting

    then:

    RuntimeException e = thrown()

    e.message.contains "Exception constructing service"
    e.message.contains "Unable to locate any service assignable to type org.apache.tapestry5.ioc.test.Greeter with marker annotation(s) org.apache.tapestry5.ioc.test.YellowMarker."
  }

  def "recursion handling injections (due to MasterObjectProvider) is detected"() {

    buildRegistry CyclicMOPModule

    def trigger = getService "Trigger", Runnable

    when:

    trigger.run()

    then:

    RuntimeException e = thrown()

    e.message.contains "Construction of service 'TypeCoercer' has failed due to recursion"
  }

  def "A field may be annotated with @InjectResource to receive resources"() {

    buildRegistry FieldResourceInjectionModule

    when:

    def s = getService FieldResourceService

    then:

    s.serviceId == "FieldResourceService"

    s.labels == ["Barney", "Betty", "Fred", "Wilma"]
  }

  def "methods with @PostInjection are invoked and can be passed further injections"() {
    buildRegistry PostInjectionMethodModule

    when:

    def g = getService Greeter

    then:

    g.greeting == "Greetings from ServiceIdGreeter."
  }

  def "a service may be overridden by contributing to ServiceOverride"() {
    buildRegistry GreeterServiceOverrideModule

    when:

    def g = getObject Greeter, null

    then:

    g.greeting == "Override Greeting"
  }

}
