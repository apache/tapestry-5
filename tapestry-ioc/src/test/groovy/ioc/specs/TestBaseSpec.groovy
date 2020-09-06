package ioc.specs

import org.apache.tapestry5.ioc.test.BeanSubclass
import org.apache.tapestry5.test.ioc.TestBase
import org.apache.tapestry5.ioc.test.Bean

import spock.lang.Specification

class TestBaseSpec extends Specification {

  TestBase base = new TestBase()

  /** Any unrecognized methods are evaluated against the TestBase instance. */
  def methodMissing(String name, args) {
    base."$name"(* args)
  }


  def "create an instance of an arbitrary class"() {
    when:

    def b = create(Bean, "value", "Magic")

    then:

    b.value == "Magic"
  }

  def "reporting of exception when instantiating an instance"() {
    when:

    create(Runnable)

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to instantiate instance of java.lang.Runnable"
  }

  def "attempt to set value of non-existent instance field of created object"() {
    when:

    create(Bean, "unknownField", "doesn't matter")

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to set field 'unknownField' of org.apache.tapestry5.ioc.test.Bean"
    e.message.contains "Class org.apache.tapestry5.ioc.test.Bean does not contain a field named 'unknownField'."
  }

  def "type mismatch when setting field value of created object"() {

    when:

    create(Bean, "value", 99)

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to set field 'value' of org.apache.tapestry5.ioc.test.Bean"
  }

  def "create object, setting fields from base class"() {
    when:

    def b = create(BeanSubclass, "flag", true, "value", "magic")

    then:

    b.flag == true
    b.value == "magic"
  }

  def "write and read a private field"() {
    def b = new Bean()
    def expected = "fred"

    when:

    set(b, "value", expected)

    then:

    b.value.is(expected)
    get(b, "value").is(expected)
  }

  def "getting the value of a field that does not exist is an error"() {
    def b = new Bean()

    when:

    get(b, "missingField")

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to read field 'missingField' of $Bean.name"
    e.message.contains "Class $Bean.name does not contain a field named 'missingField'."
  }
}
