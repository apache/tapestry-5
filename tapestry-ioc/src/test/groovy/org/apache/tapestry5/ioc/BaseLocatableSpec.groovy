package org.apache.tapestry5.ioc

import spock.lang.Specification

class LocatableFixture extends BaseLocatable {

  LocatableFixture(Location location) {
    super(location)
  }
}

class BaseLocatableSpec extends Specification {

  def "location property is readable"() {
    Location location = Mock()

    Locatable locatable = new LocatableFixture(location)

    expect:

    locatable.location.is location
  }

  def "location may be null"() {
    Locatable locatable = new LocatableFixture(null);


    expect:

    locatable.location == null
  }

}
