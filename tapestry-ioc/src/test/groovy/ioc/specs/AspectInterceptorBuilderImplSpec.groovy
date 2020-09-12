package ioc.specs

import org.apache.commons.lang3.StringUtils
import org.apache.tapestry5.ioc.services.AspectDecorator
import org.apache.tapestry5.ioc.test.internal.services.TextTransformer
import org.apache.tapestry5.plastic.MethodAdvice
import org.apache.tapestry5.plastic.MethodInvocation

import spock.lang.Shared

interface Subject {

  void advised();

  void notAdvised();
}

interface ArraysSubject {

  String[] operation(String[] inputs);
}

class AspectInterceptorBuilderImplSpec extends AbstractSharedRegistrySpecification {

  @Shared
  private AspectDecorator decorator

  def setupSpec() {
    decorator = getService AspectDecorator
  }

  def "ensure that non-advised methods are not passed through the MethodAdvice object"() {
    Subject delegate = Mock()
    MethodAdvice advice = Mock()

    def builder = decorator.createBuilder Subject, delegate, "<Subject>"

    builder.adviseMethod Subject.getMethod("advised"), advice

    Subject interceptor = builder.build()

    when:

    interceptor.advised()

    then:

    1 * advice.advise(_) >> { MethodInvocation mi ->
      assert mi.method.name == "advised"
      mi.proceed()
    }
    1 * delegate.advised()
    0 * _

    when:

    interceptor.notAdvised()

    then:

    1 * delegate.notAdvised()
    0 * _
  }

  def "failure when advising a method that is not in the service interface"() {
    Subject delegate = Mock()
    MethodAdvice advice = Mock()

    def builder = decorator.createBuilder Subject, delegate, "<Subject>"

    when:

    builder.adviseMethod Runnable.getMethod("run"), advice

    then:

    IllegalArgumentException e = thrown()

    e.message == "Method public abstract void java.lang.Runnable.run() is not defined for interface interface ioc.specs.Subject."
  }

  def "multiple advice for single method is processed in order"() {
    TextTransformer delegate = Mock()
    MethodAdvice stripFirstLetter = Mock()
    MethodAdvice reverse = Mock()

    def builder = decorator.createBuilder TextTransformer, delegate, "<TextTransformer>"

    def method = TextTransformer.getMethod "transform", String

    builder.adviseMethod method, stripFirstLetter
    builder.adviseMethod method, reverse

    TextTransformer advised = builder.build()

    when:

    def result = advised.transform "Tapestry"

    then:

    result == "[yrtsepa]"

    1 * stripFirstLetter.advise(_) >> { MethodInvocation mi ->
      assert mi.getParameter(0) == "Tapestry"
      mi.setParameter 0, mi.getParameter(0).substring(1)
      mi.proceed()
    }

    1 * reverse.advise(_) >> { MethodInvocation mi ->
      assert mi.getParameter(0) == "apestry"
      mi.setParameter 0, StringUtils.reverse(mi.getParameter(0))
      mi.proceed()
    }

    1 * delegate.transform(_) >> { it }
  }

  def "arrays are allowed as method parameters and return values"() {
    ArraysSubject delegate = Mock()
    MethodAdvice advice = Mock()

    def builder = decorator.createBuilder ArraysSubject, delegate, "unused"
    builder.adviseAllMethods advice

    ArraysSubject advised = builder.build()

    when:

    def result = advised.operation(["Fred", "Barney"] as String[])

    then:

    1 * advice.advise(_) >> { MethodInvocation it ->
      String[] inputs = it.getParameter(0)

      it.setParameter 0, inputs.collect({it.toUpperCase() }) as String[]

      it.proceed()

      def index = 0

      it.setReturnValue it.getReturnValue().collect({ value -> "${index++}:$value" }) as String[]
    }

    1 * delegate.operation(_) >> { it[0] }

    result.class == ([] as String[]).class
    result.asType(List) == ["0:FRED", "1:BARNEY"]
  }

}
