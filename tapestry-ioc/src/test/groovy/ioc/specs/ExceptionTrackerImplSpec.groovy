package ioc.specs

import org.apache.tapestry5.ioc.internal.services.ExceptionTrackerImpl
import spock.lang.Specification

class ExceptionTrackerImplSpec extends Specification {

  def "exceptions are tracked"() {

    def t1 = new RuntimeException()
    def t2 = new RuntimeException()

    when: "with a new tracker"

    def et = new ExceptionTrackerImpl()

    then: "never logged exceptions return false"

    !et.exceptionLogged(t1)
    !et.exceptionLogged(t2)

    then: "subsequently, the same exceptions return true"

    et.exceptionLogged(t1)
    et.exceptionLogged(t2)

    then: "and again"

    et.exceptionLogged(t1)
    et.exceptionLogged(t2)
  }
}
