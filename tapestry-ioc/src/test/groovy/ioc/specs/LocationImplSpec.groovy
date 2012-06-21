package ioc.specs

import org.apache.tapestry5.ioc.internal.util.ClasspathResource
import org.apache.tapestry5.ioc.internal.util.LocationImpl
import spock.lang.Shared
import spock.lang.Specification

class LocationImplSpec extends Specification {

  @Shared
  def random = new Random()

  @Shared
  def resource = new ClasspathResource("/foo/Bar.xml")

  def "toString() with all three parameters"() {
    def line = random.nextInt()
    def column = random.nextInt()

    when:

    def location = new LocationImpl(resource, line, column)

    then:

    location.resource.is(resource)
    location.line == line
    location.column == column

    location.toString() == "$resource, line $line, column $column"
  }

  def "toString() with unknown column"() {
    def line = random.nextInt()

    when:

    def location = new LocationImpl(resource, line)

    then:

    location.resource.is(resource)
    location.line == line
    location.toString() == "$resource, line $line"
  }

  def "unknown line and column"() {
    when:

    def location = new LocationImpl(resource,)

    then:

    location.resource.is(resource)
    location.toString() == resource.toString()
  }

  def "equality"() {

    when:

    def l1 = new LocationImpl(resource, 22, 7)
    def l2 = new LocationImpl(resource, 22, 7)
    def l3 = new LocationImpl(null, 22, 7)
    def l4 = new LocationImpl(resource, 99, 7)
    def l5 = new LocationImpl(resource, 22, 99)
    def l6 = new LocationImpl(new ClasspathResource("/baz/Biff.txt"), 22, 7)

    then:

    l1 == l1
    l1 != null

    l1 == l2
    l2.hashCode() == l1.hashCode()

    l3 != l1
    l3.hashCode() != l1.hashCode()

    l4 != l1
    l4.hashCode() != l1.hashCode()

    l5 != l1
    l5.hashCode() != l1.hashCode()

    l6 != l1
    l6.hashCode() != l1.hashCode()
  }
}
