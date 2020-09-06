package org.apache.tapestry5.ioc.test.internal.services

import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.ioc.ModuleBuilderSource
import org.apache.tapestry5.ioc.ServiceDecorator
import org.apache.tapestry5.ioc.ServiceResources
import org.apache.tapestry5.ioc.internal.ServiceDecoratorImpl
import org.apache.tapestry5.ioc.test.internal.FieService
import org.apache.tapestry5.ioc.test.internal.ServiceDecoratorFixture
import org.slf4j.Logger

import ioc.specs.AbstractSharedRegistrySpecification

class ServiceDecoratorImplSpec extends AbstractSharedRegistrySpecification {

  final static String SERVICE_ID = "Fie"

  ServiceDecoratorFixture fixture = new ServiceDecoratorFixture()
  ModuleBuilderSource source = { return fixture } as ModuleBuilderSource
  ServiceResources resources = Mock()
  Logger logger = Mock()
  PlasticProxyFactory proxyFactory = getService PlasticProxyFactory

  def findMethod(name) {
    def method = fixture.class.methods.find { it.name.equalsIgnoreCase(name)}

    assert method != null

    return method
  }

  def "decorator method returns an interceptor"() {
    def method = findMethod "decoratorReturnsInterceptor"
    FieService delegate = Mock()
    FieService expectedInterceptor = Mock()

    fixture.expectedDelegate = delegate
    fixture.interceptorToReturn = expectedInterceptor

    when:

    ServiceDecorator sd = new ServiceDecoratorImpl(method, source, resources, proxyFactory)

    def interceptor = sd.createInterceptor(delegate)

    then:

    1 * resources.serviceId >> SERVICE_ID
    1 * resources.serviceInterface >> FieService
    1 * resources.logger >> logger
    _ * resources.tracker >> tracker

    interceptor.is(expectedInterceptor)
  }

  def "decorator method returns null"() {
    def method = findMethod "decorateReturnNull"
    FieService delegate = Mock()

    when:

    ServiceDecorator sd = new ServiceDecoratorImpl(method, source, resources, proxyFactory)

    def interceptor = sd.createInterceptor(delegate)

    then:

    1 * resources.serviceId >> SERVICE_ID
    1 * resources.serviceInterface >> FieService
    1 * resources.logger >> logger
    _ * resources.tracker >> tracker
    1 * logger.debug(_)

    interceptor == null
  }

  def "decorator method returns instance of wrong type"() {
    def method = findMethod "decoratorUntyped"
    FieService delegate = Mock()
    Runnable wrongTypeInterceptor = Mock()

    fixture.expectedDelegate = delegate
    fixture.interceptorToReturn = wrongTypeInterceptor

    when:

    ServiceDecorator sd = new ServiceDecoratorImpl(method, source, resources, proxyFactory)

    sd.createInterceptor(delegate)

    then:

    1 * resources.serviceId >> SERVICE_ID
    1 * resources.serviceInterface >> FieService
    1 * resources.logger >> logger
    _ * resources.tracker >> tracker
    1 * logger.debug(_)

    RuntimeException e = thrown()

    e.message.contains "Decorator method org.apache.tapestry5.ioc.test.internal.ServiceDecoratorFixture.decoratorUntyped(Object)"
    e.message.contains "(invoked for service '$SERVICE_ID') returned"
    e.message.contains "which is not assignable to the org.apache.tapestry5.ioc.test.internal.FieService service interface."

  }

  def "exception thrown by decorator method is wrapped and rethrown"() {
    def method = findMethod "decoratorThrowsException"
    fixture.exception = new RuntimeException("Ouch!");

    when:

    ServiceDecorator sd = new ServiceDecoratorImpl(method, source, resources, proxyFactory)

    def interceptor = sd.createInterceptor(fixture.expectedDelegate)

    then:

    1 * resources.serviceId >> SERVICE_ID
    1 * resources.serviceInterface >> FieService
    1 * resources.logger >> logger
    _ * resources.tracker >> tracker

    1 * logger.debug(_)

    RuntimeException e = thrown()

    e.message.contains "Error invoking"
    e.message.contains "org.apache.tapestry5.ioc.test.internal.ServiceDecoratorFixture.decoratorThrowsException(java.lang.Object)"
    e.message.contains "Ouch!"

  }
}
