package ioc.specs

import org.apache.tapestry5.ioc.services.Builtin
import org.apache.tapestry5.ioc.services.TypeCoercer

import java.sql.PreparedStatement

import org.apache.tapestry5.ioc.*

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

    e.message.contains "NonAnnotatedServiceInterface id 'PeekABoo' is not defined by any module."
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

    e.message == "NonAnnotatedServiceInterface interface org.apache.tapestry5.ioc.Pingable is matched by 2 services: Barney, Fred.  Automatic dependency resolution requires that exactly one service implement the interface."
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
