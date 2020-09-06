package org.apache.tapestry5.clojure.tests

import org.apache.tapestry5.clojure.modules.ClojureModule
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.modules.TapestryModule

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

    // ClojureModule needed as contents of MANIFEST are not available at test time
    // TapestryModule needed as part of TAP5-1945
    RegistryBuilder builder = new RegistryBuilder().add(TestModule, ClojureModule, TapestryModule)

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
