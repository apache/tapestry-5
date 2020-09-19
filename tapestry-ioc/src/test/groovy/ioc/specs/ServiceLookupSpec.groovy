package ioc.specs

import java.sql.PreparedStatement

import org.apache.tapestry5.commons.AnnotationProvider
import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.ioc.services.Builtin
import org.apache.tapestry5.ioc.test.BlueMarker
import org.apache.tapestry5.ioc.test.DuplicateServiceTypeModule
import org.apache.tapestry5.ioc.test.FredModule
import org.apache.tapestry5.ioc.test.Greeter
import org.apache.tapestry5.ioc.test.GreeterModule
import org.apache.tapestry5.ioc.test.LocalModule
import org.apache.tapestry5.ioc.test.Pingable
import org.apache.tapestry5.ioc.test.StringHolder

class ServiceLookupSpec extends AbstractRegistrySpecification {

  def "access to services by id is case insensitive"() {

    buildRegistry FredModule

    when:

    def r1 = getService "Fred", Runnable
    def r2 = getService "FRED", Runnable

    then:

    r1.is r2

  }

  def "verify exception when accessing service by unknown id"() {

    buildRegistry()

    when:

    getService "PeekABoo", Runnable

    then:

    RuntimeException e = thrown()

    e.message.contains "Service id 'PeekABoo' is not defined by any module."
  }

  def "verify exception when accessing a service by type and there are no matching services"() {

    buildRegistry()

    when:

    getService PreparedStatement

    then:

    RuntimeException e = thrown()

    e.message == "No service implements the interface java.sql.PreparedStatement."
  }

  def "verify exception when accessing a service by type and there are multiple matches"() {
    buildRegistry DuplicateServiceTypeModule

    when:

    getService Pingable

    then:

    RuntimeException e = thrown()

    e.message == "Service interface org.apache.tapestry5.ioc.test.Pingable is matched by 2 services: Barney, Fred.  Automatic dependency resolution requires that exactly one service implement the interface."
  }

  def "access to builtin service via marker annotation"() {
    Builtin annotation = Mock()
    AnnotationProvider ap = Mock()

    buildRegistry()

    def tc1 = getService "TypeCoercer", TypeCoercer

    when:

    def tc2 = getObject TypeCoercer, ap

    then:

    1 * ap.getAnnotation(Builtin) >> annotation

    tc2.is tc1
  }

  def "lookup service by type and markers"() {
    buildRegistry GreeterModule

    when:

    def blue = getService Greeter, BlueMarker

    then:

    blue.greeting == "Blue"
  }

  def "use of the @Local annotation disambiguates from multiple services"() {
    buildRegistry GreeterModule, LocalModule


    when:

    // The implementation of this class relies on a @Local injection to find
    // a specific Greeter implementation

    StringHolder holder = getService "LocalGreeterHolder", StringHolder

    then:

    holder.value == "Hello, y'all!"
  }

}
