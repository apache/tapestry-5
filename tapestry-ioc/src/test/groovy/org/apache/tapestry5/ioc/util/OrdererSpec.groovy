package org.apache.tapestry5.ioc.util

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
}
