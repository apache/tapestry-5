package ioc.specs


import org.apache.tapestry5.ioc.test.internal.util.OneShotLockSubject

import spock.lang.Specification

class OneShotLockSpec extends Specification {

  def subject = new OneShotLockSubject()

  def "may only invoke locked method once"() {
    subject.go()
    subject.done()


    when:

    subject.go()

    then:

    IllegalStateException e = thrown()

    e.message.contains "${subject.class.name}.go("
    e.message.contains "may no longer be invoked"
  }

  def "the method that locks is itself checked"() {

    subject.go()
    subject.done()

    when:

    subject.done()

    then:

    IllegalStateException e = thrown()

    e.message.contains "${subject.class.name}.done("
  }
}
