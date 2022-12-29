package ioc.specs

import java.util.function.Supplier

import org.apache.tapestry5.ioc.internal.services.PerthreadManagerImpl
import org.apache.tapestry5.ioc.services.PerThreadValue
import org.slf4j.Logger

import spock.lang.Specification

class PerThreadValueSpec extends Specification {

  def manager

  def setup(){
    Logger logger = Mock()
    manager = new PerthreadManagerImpl(logger)
  }

  def "computeIfAbsent - no value"() {
    given:
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.computeIfAbsent({ newValue })
    

    then:
    perThreadValue.exists() == true
    perThreadValue.get() == newValue
  }

  def "computeIfAbsent - pre-existing value"() {
    given:
    def currentValue = "inital value"
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();
    perThreadValue.set(currentValue)

    when:
    perThreadValue.computeIfAbsent({ newValue })

    then:
    perThreadValue.exists() == true
    perThreadValue.get() == currentValue
    perThreadValue.get() != newValue
  }

  def "computeIfAbsent - supplier null"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.computeIfAbsent(null)

    then:
    thrown NullPointerException
  }

  def "computeIfPresent - no value"() {
    given:
    def currentValue = "inital value"
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.computeIfPresent({ current -> newValue })

    then:
    perThreadValue.exists() == false
    perThreadValue.get() == null
  }

  def "computeIfPresent - pre-existing value"() {
    given:
    def currentValue = "inital value"
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();
    perThreadValue.set(currentValue)

    when:
    perThreadValue.computeIfPresent({ current -> newValue })

    then:
    perThreadValue.get() != currentValue
    perThreadValue.get() == newValue
  }

  def "computeIfPresent- mapper null"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.computeIfPresent(null)

    then:
    thrown NullPointerException
  }

  def "compute - no value"() {
    given:
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.compute({ current -> newValue })

    then:
    perThreadValue.exists() == true
    perThreadValue.get() == newValue
  }

  def "compute - pre-existing value"() {
    given:
    def currentValue = "inital value"
    def newValue = "a computed value"
    PerThreadValue<String> perThreadValue = manager.createValue();
    perThreadValue.set(currentValue)

    when:
    perThreadValue.compute({ current -> newValue })

    then:
    perThreadValue.get() != currentValue
    perThreadValue.get() == newValue
  }

  def "compute - mapper null"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.compute(null)
  
    then:
    thrown NullPointerException
  }

  def "ifSet - value is set"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();
    perThreadValue.set("a value");
    def hasRun = false

    when:
    perThreadValue.ifSet { hasRun = true }

    then:
    hasRun == true
  }

  def "ifSet - no value set"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();
    def hasRun = false

    when:
    perThreadValue.ifSet { hasRun = true }

    then:
    hasRun == false
  }

  def "ifSet - action is null with no value set"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();

    when:
    perThreadValue.ifSet(null)

    then:
    thrown NullPointerException
  }

  def "ifSet - action is null with value set"() {
    given:
    PerThreadValue<String> perThreadValue = manager.createValue();
    perThreadValue.set("a value")

    when:
    perThreadValue.ifSet(null)

    then:
    thrown NullPointerException
  }

}