package ioc.specs

import java.sql.SQLException

import org.apache.tapestry5.ioc.annotations.NotLazy
import org.apache.tapestry5.ioc.services.AspectDecorator
import org.apache.tapestry5.ioc.services.LazyAdvisor
import org.apache.tapestry5.ioc.test.Greeter

public interface LazyService {

  void notLazyBecauseVoid();

  String notLazyBecauseOfReturnValue();

  /**
   * The only lazy method.
   */
  Greeter createGreeter() throws RuntimeException;

  Greeter safeCreateCreator();

  @NotLazy
  Greeter notLazyFromAnnotationGreeter();

  Greeter notLazyCreateGreeter() throws SQLException;
}

class LazyAdvisorImplSpec extends AbstractSharedRegistrySpecification {

  def LazyService advise(LazyService base) {
    def decorator = getService AspectDecorator
    def advisor = getService LazyAdvisor

    def builder = decorator.createBuilder LazyService, base, "<LazyService Proxy>"


    advisor.addLazyMethodInvocationAdvice builder

    builder.build()
  }

  LazyService service = Mock()
  LazyService advised = advise service

  def "void methods are not lazy"() {

    when:

    advised.notLazyBecauseVoid()

    then:

    service.notLazyBecauseVoid()
  }

  def "methods with a non-interface return type are not lazy"() {

    when:

    assert advised.notLazyBecauseOfReturnValue() == "so true"

    then:

    1 * service.notLazyBecauseOfReturnValue() >> "so true"
  }

  def "returned thunks cache the return value"() {

    Greeter greeter = Mock()

    when:

    def thunk = advised.createGreeter()

    then:

    0 * _

    when:

    assert thunk.greeting == "Lazy!"

    then:

    1 * service.createGreeter() >> greeter
    1 * greeter.greeting >> "Lazy!"
    0 * _

    when:

    assert thunk.greeting == "Still Lazy!"

    then: "the greeter instance is cached"

    1 * greeter.greeting >> "Still Lazy!"
    0 * _
  }

  def "a checked exception will prevent laziness"() {

    Greeter greeter = Mock()

    when:

    assert advised.notLazyCreateGreeter().is(greeter)

    then:

    1 * service.notLazyCreateGreeter() >> greeter
    0 * _
  }

  def "the @NotLazy annotation prevents laziness"() {

    Greeter greeter = Mock()

    when:

    assert advised.notLazyFromAnnotationGreeter().is(greeter)

    then:

    1 * service.notLazyFromAnnotationGreeter() >> greeter
    0 * _
  }

  def "thunk class is cached"() {

    when:

    def g1 = advised.createGreeter()
    def g2 = advised.safeCreateCreator()

    then:

    g1.class == g2.class
  }

}
