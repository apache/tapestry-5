package ioc.specs

import org.apache.tapestry5.commons.ObjectCreator
import org.apache.tapestry5.ioc.internal.ServiceActivityTracker
import org.apache.tapestry5.ioc.internal.services.JustInTimeObjectCreator
import org.apache.tapestry5.ioc.services.Status
import spock.lang.Specification

class JustInTimeObjectCreatorSpec extends Specification {

  static final String SERVICE_ID = "FooBar";

  def "can not create object after shutdown"() {

    ObjectCreator creator = Mock()

    def jit = new JustInTimeObjectCreator(null, creator, SERVICE_ID)

    // Simulate the invocation from the Registry when it shuts down.
    jit.run()

    when:

    jit.createObject()

    then:

    RuntimeException e = thrown()

    e.message.contains "Proxy for service FooBar is no longer active because the IOC Registry has been shut down."
  }

  def "lazily instantiates the object via its delegate creator"() {

    ObjectCreator creator = Mock()
    Object service = new Object()
    ServiceActivityTracker tracker = Mock()

    def jit = new JustInTimeObjectCreator(tracker, creator, SERVICE_ID)

    when:

    jit.eagerLoadService()

    then:

    1 * creator.createObject() >> service
    1 * tracker.setStatus(SERVICE_ID, Status.REAL)
    0 * _

    jit.createObject().is service
  }
}
