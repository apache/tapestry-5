package ioc.specs

import org.apache.tapestry5.ioc.test.BarneyModule
import org.apache.tapestry5.ioc.test.DecoratorList
import org.apache.tapestry5.ioc.test.FredModule
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.GreeterModule
import org.apache.tapestry5.ioc.test.GreeterModule2
import org.apache.tapestry5.ioc.test.PreventDecorationModule
import org.apache.tapestry5.ioc.test.Rocket
import org.apache.tapestry5.ioc.test.SpecificDecoratorModule
import org.apache.tapestry5.ioc.test.SpecificDecoratorModuleAgain
import org.apache.tapestry5.ioc.test.StringTransformer
import org.apache.tapestry5.ioc.test.internal.DecorateByMarkerModule
import org.apache.tapestry5.ioc.test.internal.DecorateByMarkerModule2

/** Integration tests for service decorators and some related behaviors. */
class DecoratorsSpec extends AbstractRegistrySpecification {

  def "verify order of service decorators"() {
    buildRegistry FredModule, BarneyModule

    def fred = getService "Fred", Runnable
    def list = getService DecoratorList

    when:

    fred.run()

    then:

    list.names == ["gamma", "beta", "alpha"]
  }

  def "decorators receive the delegate by the specific type"() {

    buildRegistry GreeterModule, SpecificDecoratorModule

    when:

    def g = getService "HelloGreeter", Greeter

    then:

    g.greeting == "HELLO"
  }
  
  // TAP5-1305, TAP5-2012
  def "two different modules with the same decorator method name"() {
    
    buildRegistry GreeterModule, SpecificDecoratorModule, SpecificDecoratorModuleAgain
        
    when: 
    def g = getService "HelloGreeter", Greeter
    println(g.greeting)
    
    then:
    RuntimeException e = thrown();
    e.message == "Exception constructing service 'HelloGreeter': Service HelloGreeter has two different decorators methods named decorateHelloGreeter in different module classes. You can solve this by renaming one of them and annotating it with @Match(\"HelloGreeter\").";
    
  }

  def "a service builder method with @PreventServiceDecoration is not decorated"() {
    buildRegistry PreventDecorationModule

    when:

    def st = getService StringTransformer

    then:

    st.transform("tapestry") == "TAPESTRY"
  }

  def "Binding a service with explicit no decorations will ensure that the implementation is not decorated"() {
    buildRegistry PreventDecorationModule

    when:

    def g = getService Greeter

    then:

    g.greeting == "Greetings from ServiceIdGreeter."
  }

  def "@PreventServiceDecoration on a service implementation class ensures that the implementation is not decorated"() {
    buildRegistry PreventDecorationModule

    when:

    def rocket = getService Rocket

    then:

    rocket.countdown == "3, 2, 1, Launch!"
  }

  def "@Decorate marks a module method as a decorator method"() {
    buildRegistry GreeterModule2, DecorateByMarkerModule

    when:

    def green = getService "GreenGreeter", Greeter

    then:

    green.greeting == "Decorated by foo[Decorated by baz[Decorated by bar[Green]]]"
  }

  def "@Decorate with @Local only decorates services from the same module"() {
    buildRegistry GreeterModule2, DecorateByMarkerModule

    when:

    def red = getService "RedGreeter", Greeter

    then:

    red.greeting == "Decorated by barney[Red]"
  }

  def "@Decorate with id attribute"() {
    buildRegistry DecorateByMarkerModule2

    when:

    def green = getService "RedGreeter", Greeter

    then:

    green.greeting == "Decorated by beta[Decorated by alpha[Red]]"
  }

}
