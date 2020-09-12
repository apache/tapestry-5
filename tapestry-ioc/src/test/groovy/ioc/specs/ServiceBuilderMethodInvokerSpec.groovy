package ioc.specs

import org.apache.tapestry5.commons.AnnotationProvider
import org.apache.tapestry5.commons.ObjectCreator
import org.apache.tapestry5.ioc.ServiceBuilderResources
import org.apache.tapestry5.ioc.internal.ServiceBuilderMethodInvoker
import org.apache.tapestry5.ioc.test.internal.FieService
import org.apache.tapestry5.ioc.test.internal.FoeService
import org.apache.tapestry5.ioc.test.internal.ServiceBuilderMethodFixture
import org.slf4j.Logger

class ServiceBuilderMethodInvokerSpec extends AbstractSharedRegistrySpecification {

  static String DESCRIPTION = "{CREATOR DESCRIPTION}"
  static String SERVICE_ID = "Fie"

  Logger logger = Mock()
  FieService implementation = Mock()
  ServiceBuilderResources resources = Mock()
  ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();

  def setup() {

    fixture.fie = implementation

    _ * resources.tracker >> tracker
    _ * resources.moduleBuilder >> fixture
    _ * resources.serviceId >> SERVICE_ID
    _ * resources.serviceInterface >> FieService
    _ * resources.logger >> logger
  }

  def "invoke a service builder method with no arguments"() {

    when:

    ObjectCreator oc = createObjectCreator "build_noargs"

    def actual = oc.createObject()

    then:

    actual.is implementation
  }

  def ServiceBuilderMethodInvoker createObjectCreator(methodName) {
    new ServiceBuilderMethodInvoker(resources, DESCRIPTION,
        findMethod(fixture, methodName))
  }

  def invoke(methodName) {
    createObjectCreator(methodName).createObject()
  }

  def "invoke a method with injected parameters"() {

    fixture.expectedServiceInterface = FieService
    fixture.expectedServiceResources = resources
    fixture.expectedLogger = logger

    when:

    def actual = invoke "build_args"

    then:

    actual.is implementation
  }

  def "@Inject annotation bypasses service resources when resolving value to inject"() {

    fixture.expectedString = "Injected"

    when:

    def actual = invoke "build_with_forced_injection"

    then:

    actual.is implementation

    1 * resources.getObject(String, _ as AnnotationProvider) >> "Injected"
  }

  def "@InjectService on method parameter"() {

    FoeService foe = Mock()

    fixture.expectedFoe = foe

    when:

    def actual = invoke "build_injected"

    then:

    actual.is implementation

    1 * resources.getService("Foe", FoeService) >> foe
  }

  def "@Named annotation on method parameter"() {

    FoeService foe = Mock()

    fixture.expectedFoe = foe

    when:

    def actual = invoke "build_named_injected"

    then:

    actual.is implementation

    1 * resources.getService("Foe", FoeService) >> foe
  }

  def "injection of ordered configuration as List"() {

    List<Runnable> configuration = Mock()

    fixture.expectedConfiguration = configuration

    when:

    def actual = invoke "buildWithOrderedConfiguration"

    then:

    actual.is implementation

    1 * resources.getOrderedConfiguration(Runnable) >> configuration
  }

  def "injection of unordered collection (as Collection)"() {

    Collection<Runnable> configuration = Mock()

    fixture.expectedConfiguration = configuration

    when:

    def actual = invoke "buildWithUnorderedConfiguration"

    then:

    actual.is implementation

    1 * resources.getUnorderedConfiguration(Runnable) >> configuration
  }

  def "builder method returns null"() {

    fixture.fie = null

    when:

    createObjectCreator("buildWithUnorderedConfiguration").createObject()

    then:

    RuntimeException e = thrown()

    e.message == "Builder method ${DESCRIPTION} (for service 'Fie') returned null."
  }

  def "builder method failure"() {

    when:

    createObjectCreator("build_fail").createObject()

    then:

    RuntimeException e = thrown()

    e.message.contains "build_fail()"
    e.message.contains "Method failed."

    e.cause.message == "Method failed."
  }

  def "automatically injected dependency (without an annotation)"() {

    FoeService foe = Mock()

    fixture.expectedFoe = foe

    when:

    def actual = invoke "build_auto"

    then:

    actual.is implementation

    1 * resources.getObject(FoeService, _ as AnnotationProvider) >> foe
  }
}
