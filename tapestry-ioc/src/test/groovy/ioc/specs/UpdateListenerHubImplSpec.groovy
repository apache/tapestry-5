package ioc.specs

import org.apache.tapestry5.ioc.internal.services.UpdateListenerHubImpl
import org.apache.tapestry5.ioc.services.UpdateListener
import org.apache.tapestry5.ioc.services.UpdateListenerHub

import spock.lang.Specification

import java.lang.ref.WeakReference

class UpdateListenerHubImplSpec extends Specification {

  UpdateListenerHub hub = new UpdateListenerHubImpl()

  def "add a listener and invoke it"() {

    UpdateListener listener = Mock()

    hub.addUpdateListener(listener)

    when:

    hub.fireCheckForUpdates()

    then:

    1 * listener.checkForUpdates()
  }

  def "weak references are not invoked once cleared"() {

    // Can't do this with a mock, because we hold a live reference to a mock!
    def listener = {
      throw new RuntimeException("checkForUpdates() should not be invoked on a dead reference.")
    } as UpdateListener

    WeakReference ref = new WeakReference(listener)

    hub.addUpdateListener(listener)

    // Release the only live reference and wait for GC to reclaim it
    listener = null

    while (ref.get()) { System.gc() }

    when:

    hub.fireCheckForUpdates()

    then:

    noExceptionThrown()
  }

}
