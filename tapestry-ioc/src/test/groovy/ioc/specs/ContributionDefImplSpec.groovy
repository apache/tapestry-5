package ioc.specs

import java.lang.reflect.Method

import javax.inject.Named

import org.apache.tapestry5.commons.*
import org.apache.tapestry5.ioc.*
import org.apache.tapestry5.ioc.annotations.InjectService
import org.apache.tapestry5.ioc.internal.ContributionDefImpl
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.test.internal.UpcaseService
import org.slf4j.Logger

import spock.lang.Shared
import spock.lang.Specification

class ModuleFixture {

  void contributeUnordered(Configuration configuration) {
    configuration.add(ContributionDefImplSpec.toContribute);
  }

  void contributeUnorderedInjectedService(Configuration<UpcaseService> configuration,
                                          @InjectService("Zap")
                                          UpcaseService service) {
    configuration.add(service);
  }

  void contributeUnorderedParameterNamedService(Configuration<UpcaseService> configuration,
                                                @Named("Zap")
                                                UpcaseService service) {
    configuration.add(service);
  }

  void contributeUnorderedWrongParameter(MappedConfiguration configuration) {
    throw new IllegalStateException("Unreachable.")
  }

  void contributeOrderedParameterInjectedService(OrderedConfiguration<UpcaseService> configuration,
                                                 @InjectService("Zap")
                                                 UpcaseService service) {
    configuration.add("fred", service);
  }

  void contributeMappedParameterInjectedService(MappedConfiguration<String, UpcaseService> configuration,
                                                @InjectService("Zap")
                                                UpcaseService service) {
    configuration.add("upcase", service);
  }
}


class ContributionDefImplSpec extends Specification {

  static Object toContribute

  @Shared
  OperationTracker tracker = new QuietOperationTracker()

  ServiceResources resources = Mock()
  Logger logger = Mock()

  @Shared
  ModuleBuilderSource source = new ModuleBuilderSource() {

    @Override
    Object getModuleBuilder() {
      return new ModuleFixture()
    }
  }

  private Method findMethod(name) {
    return ModuleFixture.methods.find { it.name == name }
  }

  def createContributionDef(methodName) {
    return new ContributionDefImpl("Foo", findMethod(methodName), false, null, null, null)
  }

  def "contribute to an unordered collection"() {

    Configuration configuration = Mock()

    toContribute = new Object()

    def cd = createContributionDef "contributeUnordered"

    when:

    cd.contribute(source, resources, configuration)

    then:

    1 * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    1 * configuration.add(toContribute)
    _ * resources.tracker >> tracker

    0 * _
  }

  def "unordered configuration injects and contributes a service via @InjectService"() {

    Configuration configuration = Mock()
    UpcaseService service = Mock()

    def cd = createContributionDef "contributeUnorderedInjectedService"

    when:

    cd.contribute(source, resources, configuration)

    then:

    1 * resources.getService("Zap", UpcaseService) >> service

    1 * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    1 * configuration.add(service)
    _ * resources.tracker >> tracker

    0 * _
  }

  def "unordered configuration injects and contributes a service via @Named"() {
    Configuration configuration = Mock()
    UpcaseService service = Mock()

    def cd = createContributionDef "contributeUnorderedParameterNamedService"

    when:

    cd.contribute(source, resources, configuration)

    then:

    1 * resources.getService("Zap", UpcaseService) >> service

    1 * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    1 * configuration.add(service)
    _ * resources.tracker >> tracker

  }

  def "contribution method configuration parameter must be correct type"() {
    Configuration configuration = Mock()

    def cd = createContributionDef "contributeUnorderedWrongParameter"

    when:

    cd.contribute(source, resources, configuration)

    then:

    _ * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    _ * resources.tracker >> tracker

    RuntimeException e = thrown()

    e.message.contains "Service 'Foo' is configured using org.apache.tapestry5.commons.Configuration, not org.apache.tapestry5.commons.MappedConfiguration."
  }

  def "ordered configuration injects and contributes a service via @InjectService"() {

    OrderedConfiguration configuration = Mock()
    UpcaseService service = Mock()

    def cd = createContributionDef "contributeOrderedParameterInjectedService"

    when:

    cd.contribute(source, resources, configuration)

    then:

    1 * configuration.add("fred", service)

    _ * resources.getService("Zap", UpcaseService) >> service

    _ * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    _ * resources.tracker >> tracker
  }

  def "mapped configuration injects and contributes a service via @InjectService"() {
    MappedConfiguration configuration = Mock()
    UpcaseService service = Mock()

    def cd = createContributionDef "contributeMappedParameterInjectedService"

    when:

    cd.contribute(source, resources, configuration)

    then:

    1 * configuration.add("upcase", service)

    _ * resources.getService("Zap", UpcaseService) >> service

    _ * resources.logger >> logger
    _ * resources.serviceId >> "Foo"
    _ * resources.tracker >> tracker

  }

}
