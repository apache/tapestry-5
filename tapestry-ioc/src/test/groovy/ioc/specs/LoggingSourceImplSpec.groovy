package ioc.specs

import org.apache.tapestry5.ioc.LoggerSource
import org.apache.tapestry5.ioc.internal.LoggerSourceImpl
import org.slf4j.LoggerFactory
import spock.lang.Specification

class LoggingSourceImplSpec extends Specification {

  LoggerSource loggerSource = new LoggerSourceImpl()

  def "get logger by class"() {
    Class clazz = getClass()

    expect:

    loggerSource.getLogger(clazz).is(LoggerFactory.getLogger(clazz))
  }

  def "get logger by name"() {
    String name = "foo.Bar"

    expect:

    loggerSource.getLogger(name).is(LoggerFactory.getLogger(name))

  }


}
