package ioc.specs

import org.apache.tapestry5.ioc.services.LoggingDecorator
import org.apache.tapestry5.ioc.test.internal.UpcaseService
import org.apache.tapestry5.ioc.test.internal.services.ToStringService
import org.slf4j.Logger
import org.xml.sax.SAXParseException

interface AdderService {

  long add(long operand1, long operand2);
}

interface ExceptionService {

  void parse() throws SAXParseException;
}

class LoggingDecoratorImplSpec extends AbstractSharedRegistrySpecification {

  LoggingDecorator decorator = getService LoggingDecorator

  Logger logger = Mock()

  def "logging of void method"() {

    _ * logger.debugEnabled >> true

    Runnable delegate = Mock()

    Runnable interceptor = decorator.build(Runnable, delegate, "foo.Bar", logger)

    when:

    interceptor.run()

    then:

    1 * logger.debug("[ENTER] run()")

    then:

    1 * delegate.run()

    then:

    1 * logger.debug("[ EXIT] run")

    interceptor.toString() == "<Logging interceptor for foo.Bar(java.lang.Runnable)>"
  }

  def "runtime exception inside method is logged"() {
    _ * logger.debugEnabled >> true

    Runnable delegate = Mock()

    Runnable interceptor = decorator.build(Runnable, delegate, "foo.Bar", logger)

    def t = new RuntimeException("From delegate.")

    when:

    interceptor.run()

    then:

    1 * logger.debug("[ENTER] run()")

    then:

    1 * delegate.run() >> {
      throw t
    }

    then:

    1 * logger.debug("[ FAIL] run -- ${RuntimeException.name}", t)

    then:

    RuntimeException e = thrown()

    e.is t
  }

  def "method throws checked exception"() {
    Throwable t = new SAXParseException("From delegate.", null)
    _ * logger.debugEnabled >> true
    ExceptionService delegate = Mock()

    ExceptionService service = decorator.build(ExceptionService, delegate, "MyService", logger)

    when:

    service.parse()

    then:

    Throwable actual = thrown()

    actual.is(t)

    1 * logger.debug("[ENTER] parse()")

    1 * delegate.parse() >> { throw t }

    1 * logger.debug("[ FAIL] parse -- ${SAXParseException.name}", t)
  }

  def "handling of object parameter and return type"() {
    _ * logger.debugEnabled >> true

    UpcaseService delegate = Mock()

    UpcaseService service = decorator.build(UpcaseService, delegate, "MyService", logger)

    when:

    assert service.upcase("barney") == "BARNEY"

    then:

    1 * logger.debug('[ENTER] upcase("barney")')

    1 * delegate.upcase(_) >> { args -> args[0].toUpperCase() }

    1 * logger.debug('[ EXIT] upcase ["BARNEY"]')
  }

  def "handling of primitive parameter and return type"() {
    _ * logger.debugEnabled >> true

    AdderService delegate = Mock()

    AdderService service = decorator.build(AdderService, delegate, "Adder", logger)

    when:

    assert service.add(6, 13) == 19

    then:

    1 * logger.debug("[ENTER] add(6, 13)")

    1 * delegate.add(_, _) >> { args -> args[0] + args[1] }

    1 * logger.debug("[ EXIT] add [19]")
  }

  def "toString() method of service interface is delegated"() {
    _ * logger.debugEnabled >> true

    // Spock's Mocking doesn't seem to be as savvy as Tapestry's about letting toString()
    // delegate through, so we can't implement ToStringService as a Mock

    ToStringService delegate = new ToStringService() {

      String toString() { "FROM DELEGATE" }
    }

    ToStringService service = decorator.build(ToStringService, delegate, "ToString", logger)

    when:

    assert service.toString() == "FROM DELEGATE"

    then:

    1 * logger.debug("[ENTER] toString()")
    1 * logger.debug('[ EXIT] toString ["FROM DELEGATE"]')
  }

}
