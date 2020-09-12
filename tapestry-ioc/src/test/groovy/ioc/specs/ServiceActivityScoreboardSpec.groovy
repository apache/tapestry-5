package ioc.specs

import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard
import org.apache.tapestry5.ioc.services.Status
import org.apache.tapestry5.ioc.test.BlueMarker
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.GreeterModule
import org.apache.tapestry5.ioc.test.PerThreadModule
import org.apache.tapestry5.ioc.test.RedMarker
import org.apache.tapestry5.ioc.test.StringHolder

class ServiceActivityScoreboardSpec extends AbstractRegistrySpecification {

  def "general cursory test"() {
    buildRegistry GreeterModule

    def scoreboard = getService ServiceActivityScoreboard

    when:

    def tc = getService TypeCoercer

    tc.coerce "123", Integer

    getService "BlueGreeter", Greeter

    then:

    def activity = scoreboard.serviceActivity

    !activity.empty

    activity.find({ it.serviceId == "TypeCoercer" }).status == Status.REAL

    def ppf = activity.find { it.serviceId == "PlasticProxyFactory" }
    ppf.status == Status.BUILTIN


    def rg = activity.find { it.serviceId == "RedGreeter1" }
    rg.status == Status.DEFINED
    rg.markers.contains RedMarker
    !rg.markers.contains(BlueMarker)

    def bg = activity.find { it.serviceId == "BlueGreeter"}

    bg.status == Status.VIRTUAL
    bg.markers.contains BlueMarker
    !bg.markers.contains(RedMarker)
  }

  def "scoreboard entry for perthread services is itself perthread"() {

    buildRegistry GreeterModule, PerThreadModule

    def scoreboard = getService ServiceActivityScoreboard

    def holder = getService StringHolder

    when:

    Thread t = new Thread({
      holder.value = "barney"
      assert holder.value == "barney"

      assert scoreboard.serviceActivity.find({ it.serviceId == "StringHolder"}).status == Status.REAL

      cleanupThread()
    })

    t.start()
    t.join()

    then:

    scoreboard.serviceActivity.find({ it.serviceId == "StringHolder"}).status == Status.VIRTUAL


  }
}
