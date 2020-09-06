package ioc.specs

import org.apache.tapestry5.ioc.OperationTracker
import org.apache.tapestry5.ioc.internal.OperationException;
import org.apache.tapestry5.ioc.internal.OperationTrackerImpl;
import org.slf4j.LoggerFactory;

import spock.lang.Issue;
import spock.lang.Specification

class OperationTrackerSpec extends Specification {


  @Issue('TAP5-2486')
  def "IOOperation descriptions are reported"() {
    setup:
    def logger = LoggerFactory.getLogger(OperationTracker)
    def operationTracker = new OperationTrackerImpl(logger)

    when:
    operationTracker.perform 'Throwing exception', {
      throw new IOException()
    }

    then:
    OperationException ex = thrown()
    ex.trace == ['Throwing exception']
    
  }


}
