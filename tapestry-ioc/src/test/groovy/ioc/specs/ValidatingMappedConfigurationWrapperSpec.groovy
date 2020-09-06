package ioc.specs

import java.sql.SQLException

import org.apache.tapestry5.commons.MappedConfiguration
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.ioc.def.ContributionDef
import org.apache.tapestry5.ioc.internal.ContributionDefImpl
import org.apache.tapestry5.ioc.internal.TypeCoercerProxy
import org.apache.tapestry5.ioc.internal.ValidatingMappedConfigurationWrapper

class ValidatingMappedConfigurationWrapperSpec extends AbstractSharedRegistrySpecification {

  static String SERVICE_ID = "Baz"

  ObjectLocator locator = Mock()
  TypeCoercerProxy tc = Mock()
  Map keyToContribution = [:]
  Map map = [:]

  def "contribute a property key and value"() {
    ContributionDef cd = Mock()
    def keyToContribution = [:]
    ObjectLocator locator = Mock()
    def map = [:]
    TypeCoercerProxy tc = Mock()
    Runnable value = Mock()

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, cd, Class, keyToContribution)

    when:

    config.add(Integer, value)

    then:

    tc.coerce(value, Runnable) >> value

    map[Integer].is(value)
    keyToContribution[Integer].is(cd)
  }

  def "an added value may be coerced to the correct type"() {

    ContributionDef cd = Mock()
    def value = "coerce-me"
    Runnable coerced = Mock()

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, cd, Class, keyToContribution)

    when:

    config.add(Integer, value)

    then:

    tc.coerce(value, Runnable) >> coerced

    map[Integer].is(coerced)
    keyToContribution[Integer].is(cd)
  }

  def ContributionDef newContributionDef(methodName) {

    def proxyFactory = getService PlasticProxyFactory

    return new ContributionDefImpl(SERVICE_ID, findMethod(methodName), false, proxyFactory, null, null);
  }

  def findMethod(name) {
    return this.class.methods.find() { it.name == name }
  }


  public void contributionPlaceholder1() {

  }

  public void contributionPlaceholder2() {

  }

  def "may not contribute a duplicate key"() {
    ContributionDef def1 = newContributionDef "contributionPlaceholder1"
    ContributionDef def2 = newContributionDef "contributionPlaceholder2"

    keyToContribution[Integer] = def1

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, def2, Class, keyToContribution)

    when:

    config.add(Integer, "does-not-matter")

    then:

    IllegalArgumentException e = thrown()

    e.message.contains "Service contribution (to service 'Baz') for key 'class java.lang.Integer' conflicts with existing contribution"

    keyToContribution[Integer].is(def1)
    map.isEmpty()
  }

  def "the contributed key may not be null"() {
    ContributionDef cd = newContributionDef "contributionPlaceholder1"

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, cd, Class, keyToContribution)

    when:

    config.add(null, "does-not-matter")

    then:

    NullPointerException e = thrown()

    e.message == "Key for service contribution (to service '$SERVICE_ID') was null."
  }

  def "adding a key of the wrong type is an exception"() {
    ContributionDef cd = newContributionDef "contributionPlaceholder1"

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, cd, Class, keyToContribution)

    when:

    config.add("java.util.List", "does-not-matter")

    then:

    IllegalArgumentException e = thrown()

    e.message == "Key for service contribution (to service 'Baz') was an instance of java.lang.String, but the expected key type was java.lang.Class."
  }

  def "contributing a null value is an exception"() {
    ContributionDef cd = newContributionDef "contributionPlaceholder1"

    MappedConfiguration config = new ValidatingMappedConfigurationWrapper(Runnable, locator, tc, map, null, SERVICE_ID, cd, Class, keyToContribution)

    when:

    config.add(SQLException, null)

    then:

    NullPointerException e = thrown()

    e.message == "Service contribution (to service 'Baz') was null."
    map.isEmpty()
  }
}