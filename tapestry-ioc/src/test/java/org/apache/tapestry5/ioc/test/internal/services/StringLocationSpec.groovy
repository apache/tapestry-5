package org.apache.tapestry5.ioc.test.internal.services

import org.apache.tapestry5.commons.Location
import org.apache.tapestry5.commons.internal.services.StringLocation

import spock.lang.Specification

class StringLocationSpec extends Specification {

  def "access to properties"() {
    def description = "location description", line = 909

    when:
    Location l = new StringLocation(description, line)

    then:

    l.toString().is(description)
    l.line == line
    l.column == 0
    l.resource == null
  }
}
