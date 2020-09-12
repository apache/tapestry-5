package ioc.specs

import org.apache.tapestry5.commons.internal.util.InheritanceSearch
import org.apache.tapestry5.ioc.test.internal.util.Drivable
import org.apache.tapestry5.ioc.test.internal.util.DrivableImpl
import org.apache.tapestry5.ioc.test.internal.util.Playable
import org.apache.tapestry5.ioc.test.internal.util.PlayableImpl
import org.apache.tapestry5.ioc.test.internal.util.ToyTruck
import org.apache.tapestry5.ioc.test.internal.util.ToyTruckImpl
import org.apache.tapestry5.plastic.PlasticUtils

import spock.lang.Specification
import spock.lang.Unroll

class InheritanceSearchSpec extends Specification {

  def "remove() is always a failure"() {
    when:

    new InheritanceSearch(Object).remove()

    then:

    thrown(UnsupportedOperationException)
  }

  def "exception thrown when invoking next() after Object has been reached"() {
    def s = new InheritanceSearch(Object)

    expect:

    s.next() == Object
    !s.hasNext()

    when:

    s.next()

    then:

    thrown(IllegalStateException)
  }

  @Unroll
  def "inheritance of #className is #expectedNames"() {
    def search = new InheritanceSearch(clazz)
    def result = []
    while (search.hasNext()) {
      result << search.next()
    }

    expect:

    result == expected

    where:

    clazz        | expected
    Object       | [Object]
    String       | [String, Serializable, Comparable, CharSequence, Object]
    Comparable   | [Comparable, Object]
    ToyTruck     | [ToyTruck, Playable, Drivable, Object]
    ToyTruckImpl | [ToyTruckImpl, PlayableImpl, DrivableImpl, Drivable, ToyTruck, Playable, Object]
    long         | [long, Long, Number, Comparable, Serializable, Object]
    void         | [void, Object]
    long[]       | [long[], Cloneable, Serializable, Object]
    int[][]      | [int[][], Cloneable, Serializable, Object]
    String[]     | [String[], Object[], Cloneable, Serializable, Object]
    String[][]   | [String[][], Object[], Cloneable, Serializable, Object]

    className = PlasticUtils.toTypeName(clazz)
    expectedNames = expected.collect { PlasticUtils.toTypeName(it) }.join(", ")

  }
}