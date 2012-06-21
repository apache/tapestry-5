package ioc.specs

import org.apache.tapestry5.ioc.internal.services.MethodIterator
import org.apache.tapestry5.ioc.internal.services.MethodSignature
import spock.lang.Specification
import spock.lang.Unroll

interface Play extends Runnable {

  void jump()
}

interface Runnable2 {

  void run()
}

interface Runnable3 extends Runnable, Runnable2 {

}

interface Openable {

  public void open();
}

interface OpenableWithError {

  public void open() throws IOException;
}

interface CombinedOpeneable extends Openable, OpenableWithError {
}

class MethodIteratorSpec extends Specification {

  def "iterate a simple (single-method) interface"() {

    MethodIterator mi = new MethodIterator(Runnable)

    expect:

    mi.hasNext()

    when: "iterate to first method"

    def actual = mi.next()

    then: "first method signature returned"

    actual == new MethodSignature(void, "run", null, null)

    !mi.hasNext()

    when: "iterating when no method signatures left"

    mi.next()

    then: "throws exception"

    thrown(NoSuchElementException)
  }

  def "method inherited from super interface are visible"() {

    MethodIterator mi = new MethodIterator(Play)

    expect:

    mi.hasNext()

    mi.next() == new MethodSignature(void, "jump", null, null)

    mi.hasNext()

    mi.next() == new MethodSignature(void, "run", null, null)

    !mi.hasNext()
  }

  @Unroll
  def "getToString() on #interfaceType.name should be #expected"() {

    expect:

    new MethodIterator(interfaceType).getToString() == expected

    where:

    interfaceType | expected
    Runnable      | false
    Play          | false
    ToString      | true
  }

  def "method duplicated from a base interface into a sub interface are filtered out"() {
    MethodIterator mi = new MethodIterator(Runnable3)

    expect:

    mi.next() == new MethodSignature(void, "run", null, null)
    !mi.hasNext()
  }

  def "inherited methods are filtered out if less specific"() {
    MethodIterator mi = new MethodIterator(CombinedOpeneable)

    expect:

    mi.next() == new MethodSignature(void, "open", null, [IOException] as Class[])

    !mi.hasNext()
  }

}
