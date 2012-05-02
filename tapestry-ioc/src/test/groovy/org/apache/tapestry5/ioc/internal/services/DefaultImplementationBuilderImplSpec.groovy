package org.apache.tapestry5.ioc.internal.services

import org.apache.tapestry5.ioc.AbstractSharedRegistrySpecification
import org.apache.tapestry5.ioc.services.DefaultImplementationBuilder

/** An interface that includes toString() */
interface ToString {
  String toString();
}

class DefaultImplementationBuilderImplSpec extends AbstractSharedRegistrySpecification {

  DefaultImplementationBuilder builder = getService(DefaultImplementationBuilder)

  def "default simple interface does nothing"() {
    Runnable r = builder.createDefaultImplementation(Runnable)

    when:

    r.run()

    then:

    assert r.toString() == "<NoOp java.lang.Runnable>"
  }

  def "when toString() is part of interface, the default returns null"() {
    ToString ts = builder.createDefaultImplementation(ToString)

    expect:

    ts.toString() == null
  }

  def "built instances are cached (by type)"() {
    Runnable r1 = builder.createDefaultImplementation(Runnable)
    Runnable r2 = builder.createDefaultImplementation(Runnable)

    expect:

    r1.is r2
  }

}
