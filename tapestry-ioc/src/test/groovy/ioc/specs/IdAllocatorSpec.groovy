package ioc.specs

import org.apache.tapestry5.ioc.util.IdAllocator
import spock.lang.Specification

class IdAllocatorSpec extends Specification {

  def "id is not allocated until it is allocated"() {
    when:

    IdAllocator a = new IdAllocator()

    then:

    !a.isAllocated("name")

    when:

    def actual = a.allocateId("name")

    then:

    actual == "name"
    a.isAllocated("name")
  }

  def "repeatedly allocated ids are uniqued with a suffix"() {

    IdAllocator a = new IdAllocator()

    a.allocateId("name")

    expect:

    10.times {

      def expected = "name_$it"

      assert !a.isAllocated(expected)

      assert a.allocateId("name") == expected
    }
  }

  def "access to allocated ids"() {
    IdAllocator a = new IdAllocator()

    when:

    a.allocateId("name")

    then:

    a.allocatedIds == ["name"]

    when:

    a.allocateId("name")

    then:

    a.allocatedIds == ["name", "name_0"]
  }

  def "allocation using a namespace"() {

    IdAllocator a = new IdAllocator("_NS")

    expect:

    a.allocateId("name") == "name_NS"

    a.allocateId("name") == "name_NS_0"

    // This is current behavior, but is probably something
    // that could be improved.

    a.allocateId("name_NS") == "name_NS_NS"

    a.allocateId("name_NS") == "name_NS_NS_0"
  }

  def "degenerate id allocation"() {
    IdAllocator a = new IdAllocator()

    expect:

    a.allocateId("d_1") == "d_1"
    a.allocateId("d") == "d"
    a.allocateId("d") == "d_0"
    a.allocateId("d") == "d_2"

    a.allocateId("d") == "d_3"

    // It's a collision, so a unique number is appended.
    a.allocateId("d_1") == "d_1_0"
  }

  def "degenerate id allocation (with a namespace)"() {

    IdAllocator a = new IdAllocator("_NS")

    expect:

    a.allocateId("d_1") == "d_1_NS"

    a.allocateId("d") == "d_NS"
    a.allocateId("d") == "d_NS_0"
    a.allocateId("d") == "d_NS_1"
    a.allocateId("d") == "d_NS_2"
    a.allocateId("d") == "d_NS_3"

    a.allocateId("d_1") == "d_1_NS_0"

    // This is very degenerate, and maybe something that needs fixing.

    a.allocateId("d_1_NS") == "d_1_NS_NS"
  }

  def "clearing an allocator forgets prior ids"() {
    when:

    IdAllocator a = new IdAllocator()


    then:

    a.allocateId("foo") == "foo"
    a.allocateId("foo") == "foo_0"

    when:

    a.clear()

    then:

    a.allocateId("foo") == "foo"
    a.allocateId("foo") == "foo_0"
  }

  def "cloning an id allocator does not share data with the new allocator"() {

    when:

    IdAllocator a = new IdAllocator();

    then:

    a.allocateId("foo") == "foo"
    a.allocateId("foo") == "foo_0"

    when:

    IdAllocator b = a.clone()

    then:

    ["bar", "baz", "foo", "foo"].each {
      assert a.allocateId(it) == b.allocateId(it)
    }
  }

}
