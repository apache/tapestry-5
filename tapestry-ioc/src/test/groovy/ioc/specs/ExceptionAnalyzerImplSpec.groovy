package ioc.specs

import org.apache.tapestry5.commons.Location
import org.apache.tapestry5.commons.internal.util.TapestryException
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer

class WriteOnlyPropertyException extends Exception {

  private String code;

  public String getCode() {
    return code;
  }

  public void setFaultCode(int code) {
    this.code = String.format("%04d", code);
  }
}

class SelfCausedException extends RuntimeException {

  SelfCausedException(String message) {
    super(message);
  }

  public Throwable getCause() {
    return this;
  }
}

class ExceptionAnalyzerImplSpec extends AbstractSharedRegistrySpecification {

  ExceptionAnalyzer analyzer = getService(ExceptionAnalyzer)

  def "analysis of a simple exception"() {
    when:
    def ea = analyzer.analyze(t)

    then:

    ea.exceptionInfos.size() == 1

    def ei = ea.exceptionInfos[0]

    ei.className == RuntimeException.name
    ei.message == message

    ei.propertyNames.empty
    !ei.stackTrace.empty

    where:

    message = "Hey! We've Got No Tomatoes"
    t = new RuntimeException(message)
  }

  def "access to properties of exception"() {
    Location l = Mock()
    def t = new TapestryException("Message", l, null)

    when:
    def ea = analyzer.analyze(t)

    then:

    ea.exceptionInfos.size() == 1

    def ei = ea.exceptionInfos[0]

    ei.propertyNames == ["location"]
    ei.getProperty("location").is(l)
  }

  def "access to nested exceptions"() {
    when:

    def ea = analyzer.analyze(outer)

    then:

    ea.exceptionInfos.size() == 2

    def ei = ea.exceptionInfos[0]

    ei.message == "Outer"
    ei.stackTrace.empty

    when:

    ei = ea.exceptionInfos[1]

    then:

    ei.message == "Inner"
    !ei.stackTrace.empty

    where:

    inner = new RuntimeException("Inner")
    outer = new RuntimeException("Outer", inner)
  }

  def "middle exception that adds no value is removed"() {
    when:

    def ea = analyzer.analyze(outer)

    then:

    ea.exceptionInfos.size() == 2

    def ei = ea.exceptionInfos[0]

    ei.message == "Outer: Middle"
    ei.stackTrace.empty

    when:

    ei = ea.exceptionInfos[1]

    then:

    ei.message == "Inner"

    !ei.stackTrace.empty

    where:

    inner = new RuntimeException("Inner");
    middle = new RuntimeException("Middle", inner);
    outer = new RuntimeException("Outer: Middle", middle);
  }

  def "a middle exception that adds extra information is retained"() {
    Location l = Mock()
    def inner = new RuntimeException("Inner");
    def middle = new TapestryException("Middle", l, inner);
    def outer = new RuntimeException("Outer: Middle", middle);

    when:

    def ea = analyzer.analyze(outer)

    then:

    ea.exceptionInfos.size() == 3

    def ei = ea.exceptionInfos[0]

    ei.message == "Outer: Middle"
    ei.stackTrace.empty

    when:

    ei = ea.exceptionInfos[1]

    then:

    ei.message == "Middle"
    ei.getProperty("location").is(l)
    ei.stackTrace.empty

    when:

    ei = ea.exceptionInfos[2]

    then:

    ei.message == "Inner"
    !ei.stackTrace.empty
  }

  def "write only properties are omitted"() {
    WriteOnlyPropertyException ex = new WriteOnlyPropertyException();

    ex.setFaultCode(99);

    when:

    def ea = analyzer.analyze(ex);

    then:

    def ei = ea.exceptionInfos[0]

    ei.propertyNames.contains("code")
    !ei.propertyNames.contains("faultCode")
    ei.getProperty("code") == "0099"
  }

  def "an exception that is its own cause does not cause an endless loop"() {
    when:

    def ea = analyzer.analyze(t)

    then:

    ea.exceptionInfos.size() == 1

    def ei = ea.exceptionInfos[0]

    ei.className == SelfCausedException.name
    ei.message == message

    !ei.propertyNames.contains("cause")

    !ei.stackTrace.empty

    where:

    message = "Who you lookin at?"
    t = new SelfCausedException(message)
  }
}
