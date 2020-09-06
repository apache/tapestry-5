package ioc.specs

import org.apache.tapestry5.ioc.test.AdviceDemoModule
import org.apache.tapestry5.ioc.test.AdviceMethodMissingAdvisorParameterModule
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.GreeterModule2
import org.apache.tapestry5.ioc.test.NonVoidAdvisorMethodModule
import org.apache.tapestry5.ioc.test.internal.AdviseByMarkerModule
import org.apache.tapestry5.ioc.test.internal.AdviseByMarkerModule2

class AdvisorsSpec extends AbstractRegistrySpecification {

  def "advisor methods must return void"() {
    when:

    buildRegistry NonVoidAdvisorMethodModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Advise method org.apache.tapestry5.ioc.test.NonVoidAdvisorMethodModule.adviseFoo(MethodAdviceReceiver)"
    e.message.contains "does not return void"
  }

  def "advisor methods must take a MethodAdviceReceiver parameter"() {
    when:

    buildRegistry AdviceMethodMissingAdvisorParameterModule

    then:

    RuntimeException e = thrown()

    e.message.contains "Advise method org.apache.tapestry5.ioc.test.AdviceMethodMissingAdvisorParameterModule.adviseBar()"
    e.message.contains "must take a parameter of type org.apache.tapestry5.ioc.MethodAdviceReceiver."
  }

  def "adding advice to services"() {
    buildRegistry AdviceDemoModule

    when:

    def g = getService Greeter

    then:

    g.greeting == "ADVICE IS EASY!"
  }

  def "methods marked with @Advise are advisor methods"() {

    buildRegistry GreeterModule2, AdviseByMarkerModule

    when:

    def green = getService "GreenGreeter", Greeter

    then:

    green.greeting == "gamma[beta[alpha[Green]]]"
  }

  def "@Advise with @Local only advises services in the same module"() {
    buildRegistry GreeterModule2, AdviseByMarkerModule

    when:

    def red = getService "RedGreeter", Greeter

    then:

    red.greeting == "delta[Red]"
  }

  def "@Advise with id attribute"() {
    buildRegistry AdviseByMarkerModule2

    when:

    def red = getService "RedGreeter", Greeter

    then:

    red.greeting == "beta[alpha[Red]]"
  }
}
