package org.apache.tapestry5.clojure.tests

import org.apache.tapestry5.clojure.ClojureModule
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Integration test for {@link org.apache.tapestry5.clojure.ClojureBuilder} service.
 */
class ClojureBuilderSpec extends Specification {

  @Shared @AutoCleanup("shutdown")
  Registry registry;

  @Shared
  Fixture fixture;

  def setupSpec() {

    RegistryBuilder builder = new RegistryBuilder().add(TestModule, ClojureModule)

    registry = builder.build();

    registry.performRegistryStartup();

    fixture = registry.getService(Fixture)
  }

  def "invoke a method within the namespace"() {
    expect:

    fixture.doubler(80) == 160
  }

  def "invoke a method mapped to a @FunctionName identified function"() {
    expect:

    fixture.first(["fred", "barney"]) == "fred"
  }
}
