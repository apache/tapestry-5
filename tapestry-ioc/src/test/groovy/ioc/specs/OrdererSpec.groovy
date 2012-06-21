package ioc.specs

import org.apache.tapestry5.ioc.Orderable
import org.apache.tapestry5.ioc.internal.util.Orderer
import org.apache.tapestry5.ioc.internal.util.UtilMessages
import org.slf4j.Logger
import spock.lang.Specification

class OrdererSpec extends Specification {

  Logger logger = Mock()

  def "the order of the values is unchanged when there are no dependencies"() {

    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    orderer.ordered == ["FRED", "BARNEY", "WILMA", "BETTY"]
  }

  def "an override can change order and value"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY"
      add "wilma", "WILMA"
      add "betty", "BETTY"

      override "barney", "Mr. Rubble", "before:*"
    }

    then:

    orderer.ordered == ["Mr. Rubble", "FRED", "WILMA", "BETTY"]
  }

  def "an override must match a previously added id"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY"
      add "wilma", "WILMA"
      add "betty", "BETTY"

      override "bambam", "Mr. Rubble JR.", "before:*"
    }

    then:

    IllegalArgumentException e = thrown()

    e.message == "Override for object 'bambam' is invalid as it does not match an existing object."
  }

  def "a missing constraint type is logged as a warning"() {

    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY", "fred"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    logger.warn(UtilMessages.constraintFormat("fred", "barney"))

    orderer.ordered == ["FRED", "BARNEY", "WILMA", "BETTY"]
  }

  def "an unknown constraint type is logged as a warning"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY", "nearby:fred"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    logger.warn(UtilMessages.constraintFormat("nearby:fred", "barney"))

    orderer.ordered == ["FRED", "BARNEY", "WILMA", "BETTY"]
  }

  def "null values are not included in the result"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY"
      add "zippo", null
      add "wilma", "WILMA"
      add "groucho", null
      add "betty", "BETTY"
    }

    then:

    orderer.ordered == ["FRED", "BARNEY", "WILMA", "BETTY"]
  }

  def "duplicate ids are ignored"() {
    def orderer = new Orderer(logger)

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY"
      add "wilma", "WILMA"
    }

    when:

    orderer.add("Fred", "Fred 2")

    then:

    // Notice it uses the previously added id, whose case is considered canonical
    logger.warn(UtilMessages.duplicateOrderer("fred"))

    when:

    orderer.add "betty", "BETTY"

    then:

    orderer.ordered == ["FRED", "BARNEY", "WILMA", "BETTY"]
  }

  def "the special before:* moves the value to the front of the list"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY", "before:*"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    orderer.ordered == ["BARNEY", "FRED", "WILMA", "BETTY"]
  }

  def "the special after:* moves the value to the end of the list"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED"
      add "barney", "BARNEY", "after:*"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    // A number of factors can twiddle the order of the other elements, so we just check the last
    orderer.ordered[3] == "BARNEY"
  }

  def "use lists of pre-requisites (after:)"() {

    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED", "after:wilma"
      add "barney", "BARNEY", "after:fred,betty"
      add "wilma", "WILMA"
      add "betty", "BETTY"
    }

    then:

    orderer.ordered == ["WILMA", "FRED", "BETTY", "BARNEY"]
  }

  def "use both pre- and post-requisites (before: and after:)"() {

    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED", "after:wilma"
      add "barney", "BARNEY", "after:fred,betty"
      add "wilma", "WILMA"
      add "betty", "BETTY", "before:wilma"
    }

    then:

    orderer.ordered == ["BETTY", "WILMA", "FRED", "BARNEY"]
  }

  def "pre- and post-requisites are case-insensitive"() {
    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED", "after:WILMA"
      add "barney", "BARNEY", "after:fred,BETTY"
      add "wilma", "WILMA"
      add "betty", "BETTY", "before:Wilma"
    }

    then:

    orderer.ordered == ["BETTY", "WILMA", "FRED", "BARNEY"]
  }

  def "dependency cycles are identified and logged as warnings"() {

    def orderer = new Orderer(logger)

    when:

    orderer.with {
      add "fred", "FRED", "after:wilma"
      add "barney", "BARNEY", "after:fred,betty"
      add "wilma", "WILMA"
      add "betty", "BETTY", "before:Wilma", "after:barney"
    }

    def ordered = orderer.ordered

    then:

    1 * logger.warn("Unable to add 'barney' as a dependency of 'betty', as that forms a dependency cycle ('betty' depends on itself via 'barney'). The dependency has been ignored.")


    ordered == ["BETTY", "WILMA", "FRED", "BARNEY"]
  }

  def "Orderable has a useful toString()"() {

    when:

    def simple = new Orderable("simple", "SIMPLE")

    then:

    simple.toString() == "Orderable[simple SIMPLE]"

    when:

    def complex = new Orderable("complex", "COMPLEX", "after:foo", "before:bar")

    then:

    complex.toString() == "Orderable[complex after:foo before:bar COMPLEX]"
  }
}
